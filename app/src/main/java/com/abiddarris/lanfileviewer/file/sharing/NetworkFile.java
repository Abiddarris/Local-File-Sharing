package com.abiddarris.lanfileviewer.file.sharing;

import com.abiddarris.lanfileviewer.file.FileOperationException;
import com.abiddarris.lanfileviewer.file.Requests;
import static com.abiddarris.lanfileviewer.file.sharing.JSONRequest.*;

import android.net.Uri;
import android.util.Base64;
import com.abiddarris.lanfileviewer.file.File;
import com.gretta.util.log.Log;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

public class NetworkFile extends File {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private NetworkFileSource source;
    
    private static final String TAG = Log.getTag(NetworkFile.class);

    protected NetworkFile(NetworkFileSource source, File parent, String path, String name) {
        super(source, parent, name);
        
        setPath(path);
        
        this.source = source;
    }
    
    @Override
    protected void updateInternal(String[] requests) throws Exception{
        JSONObject request = new JSONObject()
            .putOpt(KEY_REQUEST, createRequest(requests))
            .putOpt(KEY_PATH, getPath());
        
        JSONObject response = source.sendRequest(request);
        for(String key : requests) {
            if(REQUEST_LIST_FILES.equalsIgnoreCase(key)) {
                JSONArray paths = response.optJSONArray(KEY_LIST_FILES);
                File[] files = paths != null ? pathsToFiles(paths) : null;
                put(KEY_LIST_FILES, files);
                continue;
            } else if(REQUEST_GET_FILES_TREE.equalsIgnoreCase(key)) {
                List<File> files = new ArrayList<>();
                JSONArray trees = response.optJSONArray(KEY_FILES_TREE);
                for(int i = 0; i < trees.length(); ++i) {
                    files.add(source
                        .getFile(trees.getString(i)));
                }
                put(KEY_FILES_TREE,files);
                continue;
            } else if(REQUEST_GET_LENGTH.equalsIgnoreCase(key) || 
                REQUEST_GET_LAST_MODIFIED.equalsIgnoreCase(key) || 
                REQUEST_GET_FILES_TREE_SIZE.equalsIgnoreCase(key)) {
                
                key = Requests.requestToKey(key);
                put(key, response.optLong(key));
                continue;
            }
            
            key = Requests.requestToKey(key);
            put(key, response.opt(key));
        }
    }
    
    private File[] pathsToFiles(JSONArray paths) {
        File[] files = new NetworkFile[paths.length()];
        for(int i = 0; i < paths.length(); i++) {
            String path = paths.optString(i);
            files[i] = getSource()
                        .getFile(path);
        }
        return files;
    }

    @Override
    public Uri toUri() {
        SharingDevice device = source.getDevice();
        String uri = String.format("http://%s:%s%s", 
            device.getHost().getHostAddress(), device.getPort(),
            encodePath(getPath()));
        
        return Uri.parse(uri);
    }
    
    private static String encodePath(String path) {
        try {
            String[] paths = path.split("/");
        
            StringBuilder builder = new StringBuilder();
            for(String pathPart : paths) {
                builder.append(URLEncoder.encode(pathPart, "UTF-8"))
                    .append("/");
            }       

            return builder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("cannot create uri for this file", e);
        }    
    }
    
    @Override
    public boolean makeDirs() {
        try {
            JSONObject request = new JSONObject()
                .putOpt(KEY_PATH, getPath())
                .putOpt(KEY_REQUEST, createRequest(REQUEST_MAKE_DIRECTORIES));
        
            JSONObject response = source.sendRequest(request);
        
            return response.optBoolean(KEY_MAKE_DIRECTORIES_SUCCESS);
        } catch (Exception e) {
            Log.err.log(TAG, e);
            throw new FileOperationException(e);
        }
    }
    
    @Override
    public InputStream newInputStream() throws IOException {
        if((Boolean)get(KEY_IS_DIRECTORY, REQUEST_IS_DIRECTORY)) {
            throw new IOException("cannot open a directory");
        }
        
        HttpURLConnection connection = (HttpURLConnection) new URL(toUri().toString())
            .openConnection();
        
        return connection.getInputStream();
    }
    
    @Override
    public NetworkOutputStream newOutputStream() throws IOException {
        updateDataSync(REQUEST_IS_DIRECTORY);
        if((Boolean)get(KEY_IS_DIRECTORY, REQUEST_IS_DIRECTORY)) {
            throw new IOException("cannot open a directory");
        }
        SharingDevice device = source.getDevice();
        URL outputStreamURL = new URL(String.format(
                "http://%s:%s/upload?path=%s", device.getHost().getHostAddress(), 
                device.getPort(), URLEncoder.encode(getPath(), "UTF-8")));
        
        NetworkOutputStream stream = new NetworkOutputStream(outputStreamURL);
        stream.setName("stream");
        stream.setFileName(getName());
        
        return stream;
    }
    
    @Override
    public Progress copy(File dest) {
        Progress progress = new Progress();
        executor.submit(() -> {
            try {
                JSONObject request = new JSONObject()
                    .putOpt(KEY_REQUEST, createRequest(REQUEST_COPY))
                    .putOpt(KEY_PATH, getPath())
                    .putOpt(KEY_DEST, dest.getPath());
                  
                JSONObject response = source.sendRequest(request);
                int progressId = response.optInt(KEY_PROGRESS_ID);
                  
                updateProgress(progressId, progress);
            } catch (Exception e) {
                progress.setException(e);
                progress.setCompleted(true);        
            }
        });
        return progress;
    }
    
    @Override
    public boolean rename(String newName) {
        try {
        	JSONObject request = new JSONObject()
                .put(KEY_REQUEST, createRequest(REQUEST_RENAME))
                .put(KEY_PATH, getPath())
                .put(KEY_NEW_NAME, newName);
            JSONObject response = source.sendRequest(request);
            
            return response.getBoolean(KEY_SUCESS);
        } catch(Exception err) {
        	Log.err.log(TAG,err);
            throw new FileOperationException(err);
        }
    }
    
    private void updateProgress(int progressId, Progress progress) throws Exception{
        JSONObject request = new JSONObject()
                    .putOpt(KEY_REQUEST, createRequest(REQUEST_PROGRESS))
                    .putOpt(KEY_PROGRESS_ID, progressId);
        JSONObject response = null;
        while(!progress.isCompleted()) {
            if(progress.isCancel()) {
                JSONObject cancelRequest = new JSONObject()
                    .putOpt(KEY_REQUEST, createRequest(REQUEST_CANCEL_PROGRESS))
                    .putOpt(KEY_PROGRESS_ID, progressId);
                source.sendRequest(cancelRequest);  
            } 
            response = source.sendRequest(request);
                        
            progress.setCompleted(response.optBoolean(KEY_COMPLETED));  
            progress.setCurrentProgress(response.optLong(KEY_PROGRESS));
            progress.setSize(response.optLong(KEY_LENGTH));
        }  
                    
        JSONObject removeRequest = new JSONObject()    
            .putOpt(KEY_REQUEST, createRequest(REQUEST_REMOVE_PROGRESS))
            .putOpt(KEY_PROGRESS_ID, progressId);
                
        source.sendRequest(removeRequest);   
                    
        String base64Exception = response.optString(KEY_EXCEPTION);
        if(base64Exception == null) return;
                    
        byte[] datas = Base64.decode(base64Exception, Base64.DEFAULT);
        ObjectInputStream reader = new ObjectInputStream(
            new BufferedInputStream(new ByteArrayInputStream(datas)));
        Exception e = (Exception) reader.readObject();  
        progress.setException(e);
    }
    
    @Override
    public boolean delete() {
        try {
            JSONObject request = new JSONObject()
                .put(KEY_REQUEST, createRequest(REQUEST_DELETE))
                .put(KEY_PATH, getPath());
            
            JSONObject response = source.sendRequest(request);
            return response.getBoolean(KEY_SUCESS);
        } catch (Exception err) {
            Log.err.log(TAG, err);
            throw new FileOperationException(err);
        }
    }
    
    @Override
    public Progress move(File dest) {
        Progress progress = new Progress();
        executor.submit(() -> {
            try {
                JSONObject request = new JSONObject()
                    .putOpt(KEY_REQUEST, createRequest(REQUEST_MOVE))
                    .putOpt(KEY_PATH, getPath())
                    .putOpt(KEY_DEST, dest.getPath());
                  
                JSONObject response = source.sendRequest(request);
                int progressId = response.optInt(KEY_PROGRESS_ID);
                  
                updateProgress(progressId, progress);
            } catch (Exception e) {
                progress.setException(e);
                progress.setCompleted(true);        
            }
        });
        return progress;
    }
    
    @Override
    public void createThumbnail(ThumbnailCallback callback) {
        SharingDevice device = source.getDevice();
        String host = device.getHost().getHostAddress();
        int port = device.getPort();
        
        Uri uri = Uri.parse(String.format("http://%s:%s%s?type=thumbnail", host, port, encodePath(getPath())));
        callback.onThumbnailCreated(uri);
    }
  
}
