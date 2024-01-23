package com.abiddarris.lanfileviewer.file.sharing;

import com.abiddarris.lanfileviewer.file.FileSource;
import static com.abiddarris.lanfileviewer.file.sharing.JSONRequest.*;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.file.File;
import com.gretta.util.log.Log;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkFile extends File {

    private boolean exists;
    private boolean isDirectory;
    private boolean isFile;
    private long lastModified;
    private long length;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private File parentFile;
    private File[] listFiles;
    private NetworkFileSource source;
    private String mimeType;
    private String name;
    private String path;
    private String absolutePath;
    
    private static final String TAG = Log.getTag(NetworkFile.class);

    protected NetworkFile(NetworkFileSource source, File parent, String path) {
        super(parent);
        
        this.path = path;
        this.source = source;
    }

    @Override
    public void updateData(Callback callback) {
        Log.debug.log(TAG, "Updating file : " + path);
        JSONObject request = new JSONObject();
        try {
            onCreateUpdateRequest(request);
        } catch (JSONException e) {
            Log.err.log(TAG, e);
        }

        source.sendRequest(request, (response, e) -> {
            onResponseAvailable(response);

            if (callback != null)
                ApplicationCore.getMainHandler().post((c) -> callback.onDataUpdated(e));
        });
    }
    
    public void updateDataSync() {
        Log.debug.log(TAG, "Updating sync file : " + path);
        JSONObject request = new JSONObject();
        try {
            onCreateUpdateRequest(request);
            JSONObject response = source.sendRequestSync(request);
        
            onResponseAvailable(response);    
        } catch (Exception e) {
            Log.err.log(TAG, e);
        }
    }
    
    protected void onCreateUpdateRequest(JSONObject request) throws JSONException {
        JSONArray requestKeys = createRequest(REQUEST_GET_NAME, REQUEST_LIST_FILES,
             REQUEST_IS_DIRECTORY, REQUEST_IS_FILE, REQUEST_GET_PARENT_FILE,
             REQUEST_GET_MIME_TYPE, REQUEST_GET_LENGTH, REQUEST_GET_LAST_MODIFIED, REQUEST_EXISTS, REQUEST_ABSOLUTE_PATH);
        
        request.putOpt(KEY_REQUEST, requestKeys)
                .putOpt(KEY_PATH, path);
    }

    protected void onResponseAvailable(JSONObject response) {
        name = response.optString(KEY_NAME);
        isDirectory = response.optBoolean(KEY_IS_DIRECTORY);
        isFile = response.optBoolean(KEY_IS_FILE);
        parentFile = source.getFile(response.optString(KEY_GET_PARENT_FILE));
        mimeType = response.optString(KEY_MIME_TYPE);
        length = response.optLong(KEY_LENGTH);
        lastModified = response.optLong(KEY_LAST_MODIFIED);
        exists = response.optBoolean(KEY_EXISTS);
        absolutePath = response.optString(KEY_ABSOLUTE_PATH);
        
        JSONArray paths = response.optJSONArray(KEY_LIST_FILES);
        if (paths != null) {
            listFiles = new NetworkFile[paths.length()];
            for (int i = 0; i < paths.length(); i++) {
                String path = paths.optString(i);
                
                listFiles[i] = source.getFile(path);
            }
        }
    }
    
    @Override
    public FileSource getSource() {
        return source;
    }
    
    @Override
    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean isFile() {
        return isFile;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public File[] listFiles() {
        return listFiles;
    }

    @Override
    public String getAbsolutePath() {
        return absolutePath;
    }
    
    @Override
    public String getPath() {
        return path;
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
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }
    
    @Override
    public boolean createNewFile() {
        return false;
    }
    
    @Override
    public boolean makeDirs() {
        try {
            JSONObject request = new JSONObject()
                .putOpt(KEY_PATH, path)
                .putOpt(KEY_REQUEST, createRequest(REQUEST_MAKE_DIRECTORIES));
        
            JSONObject response = source.sendRequestSync(request);
        
            updateDataSync();
            
            return response.optBoolean(KEY_MAKE_DIRECTORIES_SUCCESS);
        } catch (Exception e) {
            Log.err.log(TAG, e);
            return false;
        }
    }
    
    @Override
    public InputStream newInputStream() throws IOException {
        if(isDirectory()) {
            throw new IOException("cannot open a directory");
        }
        
        HttpURLConnection connection = (HttpURLConnection) new URL(toUri().toString())
            .openConnection();
        
        return connection.getInputStream();
    }
    
    @Override
    public NetworkOutputStream newOutputStream() throws IOException {
        if(isDirectory()) {
            throw new IOException("cannot open a directory");
        }
        SharingDevice device = source.getDevice();
        URL outputStreamURL = new URL(String.format(
                "http://%s:%s/upload?path=%s", device.getHost().getHostAddress(), 
                device.getPort(), URLEncoder.encode(path, "UTF-8")));
        
        NetworkOutputStream stream = new NetworkOutputStream(outputStreamURL);
        stream.setName("stream");
        stream.setFileName(getName());
        
        return stream;
    }
    
    @Override
    public boolean exists() {
        return exists;
    }
    
    @Override
    public Progress copy(File dest) {
        Progress progress = new Progress();
        executor.submit(() -> {
            try {
                JSONObject request = new JSONObject()
                    .putOpt(KEY_REQUEST, createRequest(REQUEST_COPY))
                    .putOpt(KEY_PATH, path)
                    .putOpt(KEY_DEST, dest.getPath());
                  
                JSONObject response = source.sendRequestSync(request);
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
                .put(KEY_PATH, path)
                .put(KEY_NEW_NAME, newName);
            JSONObject response = source.sendRequestSync(request);
            
            return response.getBoolean(KEY_SUCESS);
        } catch(Exception err) {
        	Log.err.log(TAG,err);
        }
        return false;
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
                source.sendRequestSync(cancelRequest);  
            } 
            response = source.sendRequestSync(request);
                        
            progress.setCompleted(response.optBoolean(KEY_COMPLETED));  
            progress.setCurrentProgress(response.optLong(KEY_PROGRESS));
            progress.setSize(response.optLong(KEY_LENGTH));
        }  
                    
        JSONObject removeRequest = new JSONObject()    
            .putOpt(KEY_REQUEST, createRequest(REQUEST_REMOVE_PROGRESS))
            .putOpt(KEY_PROGRESS_ID, progressId);
                
        source.sendRequestSync(removeRequest);   
                    
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
                .put(KEY_PATH, path);
            
            JSONObject response = source.sendRequestSync(request);
            return response.getBoolean(KEY_SUCESS);
        } catch (Exception err) {
            Log.err.log(TAG, err);
        }
        return false;
    }
    
    @Override
    public Progress move(File dest) {
        Progress progress = new Progress();
        executor.submit(() -> {
            try {
                JSONObject request = new JSONObject()
                    .putOpt(KEY_REQUEST, createRequest(REQUEST_MOVE))
                    .putOpt(KEY_PATH, path)
                    .putOpt(KEY_DEST, dest.getPath());
                  
                JSONObject response = source.sendRequestSync(request);
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
        
        Uri uri = Uri.parse(String.format("http://%s:%s%s?type=thumbnail", host, port, encodePath(path)));
        callback.onThumbnailCreated(uri);
    }
    
    @Override
    public synchronized List<File> getFilesTree() {
        try {
            List<File> files = new ArrayList<>();
            JSONObject request = new JSONObject()
                .put(KEY_PATH, path)
                .put(KEY_REQUEST, createRequest(REQUEST_GET_FILES_TREE));
            
            JSONObject response = source.sendRequestSync(request);
            JSONArray trees = response.optJSONArray(KEY_FILES_TREE);
            for(int i = 0; i < trees.length(); ++i) {
            	files.add(source
                    .getFile(trees.getString(i)));
            }
            return files;
        } catch (Exception e) {
            Log.err.log(TAG, e);
            return null;
        }
    }
    
    @Override
    public long getFilesTreeSize() {
        try {
            JSONObject request = new JSONObject()
                .put(KEY_PATH, path)
                .put(KEY_REQUEST, createRequest(REQUEST_GET_FILES_TREE_SIZE));
            
            JSONObject response = source.sendRequestSync(request);
            return response.optLong(KEY_FILES_TREE_SIZE);
        } catch (Exception e) {
            Log.err.log(TAG, e);
            return -1;
        }
    }
    
    
}
