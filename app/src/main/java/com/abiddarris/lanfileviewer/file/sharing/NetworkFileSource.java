package com.abiddarris.lanfileviewer.file.sharing;

import static com.abiddarris.lanfileviewer.file.sharing.JSONRequest.*;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.RootFile;
import com.abiddarris.lanfileviewer.file.RootFileContainer;
import java.util.concurrent.ExecutorService;
import java.net.URL;
import java.io.IOException;
import org.json.JSONException;
import java.util.concurrent.Executors;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;

public class NetworkFileSource extends FileSource {
    
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private RootFileContainer root;
    private SharingDevice device;
    private URL server;
    
    NetworkFileSource(SharingDevice device) throws Exception {
        this.device = device;
        
        server = new URL("http://" + device.getHost().getHostName() +
            ":" + device.getPort() + "/fetch");
        
        JSONObject request = new JSONObject()
            .put(KEY_REQUEST, JSONRequest.createRequest(REQUEST_GET_TOP_DIRECTORY_FILES));
        JSONObject response = sendRequestSync(request);
        JSONArray jsonTopDirectoryFiles = response.optJSONArray(KEY_TOP_DIRECTORY_FILES);
        root = new RootFileContainer(); 
            
        for(int i = 0; i < jsonTopDirectoryFiles.length(); ++i) {
            String path = jsonTopDirectoryFiles.optString(i);
            RootFile child = new NetworkRootFile(this, root, path);
         
            registerToCache(child); 
            root.addRoots(child);
        }
            
        registerToCache(root);
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

    @Override
    public File getRoot() {
        return root;
    }

    @Override
    protected File newFile(File parent, String path) {
        return new NetworkFile(this,path);
    }

    public static interface ResponseCallback {
        void onResponseAvailable(JSONObject json);
    }
    
    public SharingDevice getDevice() {
        return this.device;
    }
}
