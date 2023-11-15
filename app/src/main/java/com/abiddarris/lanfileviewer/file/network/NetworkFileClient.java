package com.abiddarris.lanfileviewer.file.network;

import android.net.Uri;
import static com.abiddarris.lanfileviewer.file.network.JSONRequest.*;

import android.os.Handler;
import android.os.Looper;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.FileExplorerActivity;
import com.abiddarris.lanfileviewer.file.File;
import com.gretta.util.log.Log;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkFileClient extends BaseRunnable {

    private ConnectedCallback callback;
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private InetAddress address;
    private int port;
    private NetworkFileSource source;
    private URL server;
    
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
        server = new URL("http://" + address.getHostName() +
            ":" + port + "/fetch");
        
        source = new NetworkFileSource(this);
    }

    public void sendRequest(JSONObject json, ResponseCallback callback) {
        executor.submit(() -> {
            try {
                JSONObject response = sendRequestSync(json);
                callback.onResponseAvailable(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public JSONObject sendRequestSync(JSONObject json) throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) server.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(json.toString().getBytes());
        outputStream.flush();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String data;
        StringBuilder result = new StringBuilder();
        while((data = reader.readLine()) != null) {
            result.append(data)
                .append("\n");
        }
        reader.close();
        outputStream.close();
        
        return new JSONObject(result.toString());
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public int getPort() {
        return port;
    }

    public ConnectedCallback getConnectedCallback() {
        return this.callback;
    }

    public NetworkFileSource getSource() {
        return this.source;
    }
    
    public static interface ResponseCallback {
        void onResponseAvailable(JSONObject json);
    }

    public static interface ConnectedCallback {
        void onConnected(NetworkFileSource source);
    }

    private static class ResponseHolder {
        private JSONObject response;
    }
    
}
