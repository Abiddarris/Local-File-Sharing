package com.abiddarris.lanfileviewer.file.network;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import com.abiddarris.lanfileviewer.BaseRunnable;
import com.abiddarris.lanfileviewer.ConnectionService;
import com.gretta.util.log.Log;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

public class NetworkFileServer extends BaseRunnable {

    private boolean running = true;
    private ConnectionService service;
    private ExecutorService localExecutor = Executors.newFixedThreadPool(10);
    private ServerSocket serverSocket;
    
    private static final String TAG = Log.getTag(NetworkFileServer.class);
    
    public NetworkFileServer(ConnectionService service) {
        this.service = service;
    }
    
    @Override
    public void onExecute() throws Exception {
        Log.debug.log(TAG,"Server Thread Running");
        
        serverSocket = new ServerSocket(0);
        new Handler(Looper.getMainLooper())
            .post(() -> service.onPortAvailable(serverSocket.getLocalPort()));
        
        while(running) {
            Socket socket = serverSocket.accept();
            handleSocket(socket);
        }
    }
    
    public void close() {
        Log.debug.log(TAG, "Closing server");
        
        try {
            serverSocket.close();
            localExecutor.shutdownNow();
        } catch (IOException e) {
            Log.err.log(TAG, e);
        }
    }

    private void handleSocket(final Socket socket) throws Exception {
        Log.debug.log(TAG, "Connection Established");
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String data;
        StringBuilder request = new StringBuilder();
        while((data = reader.readLine()) != null && !data.isEmpty()) {
            Log.debug.log(TAG, "line : " + data);
            request.append(data)
                .append("\n");
        }
        
        String requestData = request.toString();
        Log.debug.log(getTag(), "Request data : ");
        Log.debug.log(getTag(), requestData);
        
        RequestHandler requestHandler;
        if(requestData.startsWith("GET")) {
            requestHandler = new HTTPRequestHandler();
        } else {
            requestHandler = new JSONRequestHandler();
        }
        
        requestHandler.setInputStream(socket.getInputStream());
        requestHandler.setOutputStream(socket.getOutputStream());
        requestHandler.setRequest(requestData);
            
        localExecutor.submit(requestHandler);
    }

}
