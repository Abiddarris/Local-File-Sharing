package com.abiddarris.lanfileviewer.file.sharing;

import android.net.Uri;
import static com.abiddarris.lanfileviewer.file.sharing.JSONRequest.*;

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
    
    private InetAddress address;
    private int port;
    private NetworkFileSource source;
    
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
    
    

    public static interface ConnectedCallback {
        void onConnected(NetworkFileSource source);
    }

    private static class ResponseHolder {
        private JSONObject response;
    }
    
}
