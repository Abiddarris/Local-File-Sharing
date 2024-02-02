package com.abiddarris.lanfileviewer.file.sharing;

import static com.abiddarris.lanfileviewer.file.sharing.JSONRequest.*;

import android.content.Context;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.RootFile;
import com.gretta.util.log.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkFileSource extends FileSource {
   
    private RootFile root;
    private SharingDevice device;
    private URL server;
    
    public static final String TAG = Log.getTag(NetworkFileSource.class);
    
    NetworkFileSource(SharingDevice device, Context context) throws Exception {
        super(context);
        
        this.device = device;
        
        server = new URL("http://" + device.getHost().getHostName() +
            ":" + device.getPort() + "/fetch");
        
        JSONObject request = new JSONObject()
            .put(KEY_REQUEST, JSONRequest.createRequest(REQUEST_GET_TOP_DIRECTORY_FILES));
        JSONObject response = sendRequest(request);
        JSONArray jsonTopDirectoryFiles = response.optJSONArray(KEY_TOP_DIRECTORY_FILES);
        
        for(int i = 0; i < jsonTopDirectoryFiles.length(); ++i) {
            String path = jsonTopDirectoryFiles.optString(i);
            File child = new NetworkFile(this, getRoot().getPath(), path, getName(path));
         
            registerToRoot(child);
        }
    }
    
    public JSONObject sendRequest(JSONObject json) throws RequestException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) server.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=\"UTF-8\"");
        
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(json.toString().getBytes("UTF-8"));
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
        } catch (ProtocolException | JSONException e) {
            throw new RequestException("Failed to sent a request : ", e);
        } catch (IOException e) {
            if(connection == null) {
                throw new RequestException("Failed to sent a request : ", e);
            }
            
            Exception exception = getServerException(connection);
            Throwable cause = getCause(e);
            if(exception != cause) {
                cause.initCause(exception);
            }
          
            throw new RequestException("Failed to sent a request", e);
        } finally {
            if(connection != null) connection.disconnect();
        }
    }

    private Throwable getCause(Throwable e) {
        Throwable cause = e.getCause();
        if(cause == null) return e;
        
        return getCause(cause);
    }
    
    public Exception getServerException(HttpURLConnection connection) {
    	try {
            if(connection.getResponseCode() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
                return null;
            }
            
            String mimeType = connection.getHeaderField("Content-Type");
            if(mimeType.equalsIgnoreCase("text/plain")) {
                return new IOException("Cannot get error messages : Server cannot sent error messages");
            } else if(mimeType.equalsIgnoreCase("application/octet-stream")) {
                ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(connection.getErrorStream()));
                    Exception exception = (Exception) inputStream.readObject();
                inputStream.close();
                
                return exception;
            } else {
                return new IOException("Cannot get error messages : unrecognize content type");
            }
        } catch (IOException | ClassNotFoundException e) {
            return e;
        }
    }

    @Override
    protected File newFile(String parent, String name) {
        return new NetworkFile(this, parent, null, name);
    }
    
    public SharingDevice getDevice() {
        return this.device;
    }
}
