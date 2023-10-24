package com.abiddarris.lanfileviewer.file.network;

import static com.abiddarris.lanfileviewer.file.network.JSONRequest.*;

import android.os.Handler;
import android.os.Looper;
import com.abiddarris.lanfileviewer.BaseRunnable;
import com.abiddarris.lanfileviewer.FileExplorerActivity;
import com.abiddarris.lanfileviewer.file.File;
import com.gretta.util.log.Log;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkFileClient extends BaseRunnable implements Closeable {

    private DataOutputStream output;
    private ConnectedCallback callback;
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private InetAddress address;
    private int port;
    private NetworkFileSource source;
    private Random random = new Random();
    private Socket socket;
    private volatile Map<Integer, ResponseCallback> callbacks = new HashMap<>();
    private volatile Map<String, File> fileInfos = new HashMap<>();

    private static final String TAG = Log.getTag(NetworkFileClient.class);

    public NetworkFileClient(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public void setConnectedCallback(ConnectedCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onExecute() throws Exception {
        socket = new Socket(address, port);
        DataInputStream input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        output.write("JSON request\n\n".getBytes());
        output.flush();

        source = new NetworkFileSource(this);

        while (true) {
            JSONObject jsonObject = new JSONObject(read(input));
            int id = jsonObject.getInt(KEY_ID);
            ResponseCallback callback = releaseId(id);
            callback.onResponseAvailable(jsonObject);
        }
    }

    private String read(final DataInputStream input) throws IOException {
        StringBuilder builder = new StringBuilder();
        String data = "";
        while (!data.endsWith("[END]")) {
            data = input.readUTF();
            builder.append(data);
        }
        builder.delete(builder.length() - 5, builder.length());
        return builder.toString();
    }

    @Override
    public void onFinalization() {
        super.onFinalization();

        close();
    }

    @Override
    public void close() {
        Log.debug.log(TAG, "Closing Connection");
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                Log.err.log(TAG, e);
            }
        }
    }

    /* public File getFileInfo(String path) {
        File file = fileInfos.get(path);
        if (file != null) return file;

        file = new NetworkFile(this, path);
        fileInfos.put(path, file);

        return file;
    }*/

    public void sendRequest(JSONObject json, ResponseCallback callback) {
        executor.submit(
                () -> {
                    try {
                        Log.debug.log(TAG, "Sending a request to server");
                        int id = getId();
                        json.put(KEY_ID, id);

                        callbacks.put(id, callback);
                        output.writeUTF(json.toString());
                        output.flush();
                    } catch (Exception e) {
                        Log.debug.log(TAG, e);
                    }
                });
    }

    private ResponseCallback releaseId(int id) {
        ResponseCallback callback = callbacks.get(id);
        callbacks.remove(id);

        return callback;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public int getPort() {
        return port;
    }

    private int getId() {
        while (true) {
            int rand = random.nextInt();
            Log.debug.log(TAG, "Generating random number : " + rand);
            if (callbacks.get(random) == null) {
                Log.debug.log(TAG, "The Number is unique, returning the number!");
                return rand;
            }
        }
    }

    public static interface ResponseCallback {
        void onResponseAvailable(JSONObject json);
    }

    public static interface ConnectedCallback {
        void onConnected(NetworkFileSource source);
    }

    public ConnectedCallback getConnectedCallback() {
        return this.callback;
    }

    public NetworkFileSource getSource() {
        return this.source;
    }

    
}
