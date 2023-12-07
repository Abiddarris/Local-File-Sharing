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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkFile implements File {

    private boolean exists;
    private boolean isDirectory;
    private boolean isFile;
    private long lastModified;
    private long length;
    private File parentFile;
    private File[] listFiles;
    private NetworkFileSource source;
    private String mimeType;
    private String name;
    private String path;
    
    private static final String TAG = Log.getTag(NetworkFile.class);

    protected NetworkFile(NetworkFileSource source, String path) {
        this.path = path;
        this.source = source;
    }

    @Override
    public void updateData(Callback callback) {
        JSONObject request = new JSONObject();
        try {
            onCreateUpdateRequest(request);
        } catch (JSONException e) {
            Log.err.log(TAG, e);
        }

        source.sendRequest(request, (response) -> {
            onResponseAvailable(response);

            if (callback != null)
                ApplicationCore.getMainHandler().post(() -> callback.onDataUpdated());
        });
    }
    
    public void updateDataSync() throws Exception {
        JSONObject request = new JSONObject();
        try {
            onCreateUpdateRequest(request);
        } catch (JSONException e) {
            Log.err.log(TAG, e);
        }

        JSONObject response = source.sendRequestSync(request);
        
        onResponseAvailable(response);
    }
    
    protected void onCreateUpdateRequest(JSONObject request) throws JSONException {
        JSONArray requestKeys = createRequest(REQUEST_GET_NAME, REQUEST_LIST_FILES,
             REQUEST_IS_DIRECTORY, REQUEST_IS_FILE, REQUEST_GET_PARENT_FILE,
             REQUEST_GET_MIME_TYPE, REQUEST_GET_LENGTH, REQUEST_GET_LAST_MODIFIED, REQUEST_EXISTS);
        
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
    public File getParentFile() {
        return parentFile;
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
    public String getPath() {
        return path;
    }

    @Override
    public Uri toUri() {
        SharingDevice device = source.getDevice();
        return Uri.parse("http://" + device.getHost()
            .getHostAddress() + ":" + device.getPort() +
             "/" + getPath());
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
    
}
