package com.abiddarris.lanfileviewer.file.sharing;

import static com.abiddarris.lanfileviewer.file.network.JSONRequest.*;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.abiddarris.lanfileviewer.file.network.NetworkFileServer;
import com.gretta.util.log.Log;
import fi.iki.elonen.NanoHTTPD;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class SharingSession extends NanoHTTPD implements RegistrationListener {
    
    private Context context;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private NsdManager nsdManager;
    private NetworkFileServer serverThread;
    
    private static final String TAG = Log.getTag(SharingSession
        .class);
    
    public SharingSession(Context context) {
        super(0);
        
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }
    
    public void start() throws IOException {
        start(SOCKET_READ_TIMEOUT);
        
        NsdServiceInfo info = new NsdServiceInfo();
        info.setServiceName(Build.BRAND + " " + Build.DEVICE + "_FILEV");
        info.setServiceType(FileSharing.SERVICE_TYPE);
        info.setPort(getListeningPort());
        
        Log.debug.log(TAG, "Port Available : " + getListeningPort());

        nsdManager.registerService(info, NsdManager.PROTOCOL_DNS_SD, this);
    }
    
    public void close() {
        stop();
        
        nsdManager.unregisterService(this);
    }
    
    public Context getContext() {
        return context;
    }
    
    public boolean isRegistered() {
        return isAlive();
    }
    
    @Override
    public Response serve(IHTTPSession session) {
        try {
            if(session.getMethod() == Method.GET) {
                return getFile(session);
            }
            if(session.getMethod() == Method.POST) {
                return handlePost(session);
            }
            return null;
        } catch (Exception e) {
            Log.err.log(TAG,e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", e.toString());
        }
    }

    private Response handlePost(IHTTPSession session) throws Exception {
        String uri = session.getUri();
        if(uri.startsWith("/fetch")) {
            return handleFetch(session);
        }
        return null;
    }

    private Response handleFetch(IHTTPSession session) throws Exception {
        Map<String,String> body = new HashMap<>();
        session.parseBody(body);
        
        String requestString = body.get("postData");
        JSONObject request = new JSONObject(requestString);
        JSONObject response = new JSONObject();
        
        response.put(KEY_ID, request.getInt(KEY_ID));
        
        JSONArray requestKeys = request.getJSONArray(KEY_REQUEST);
        String path = request.optString(KEY_PATH);
        for(int i = 0; i < requestKeys.length(); ++i) {
        	String key = requestKeys.getString(i);
            if(path != null) {
                fetchFileRelated(request,response,key,path);
            }
            
            fetchOthersRequest(request,response,key,path);
        }
        
        return newFixedLengthResponse(Response.Status.OK, "application/json", response.length());
    }
    
    private void fetchOthersRequest(JSONObject request, JSONObject response, String key, String path) throws JSONException {
        if(key.equals(REQUEST_GET_TOP_DIRECTORY_FILES)) {
        	JSONArray topDirectoryFiles = new JSONArray();
            File root = LocalFileSource.getDefaultLocalSource(
                ApplicationCore.getApplication()).getRoot();
            for(File subroot : root.listFiles()) {
                topDirectoryFiles.put(subroot.getPath());
            }
 
            response.put(KEY_TOP_DIRECTORY_FILES,topDirectoryFiles);
        }
    }

    private void fetchFileRelated(JSONObject request, JSONObject response, String key, String path) throws JSONException {
        File file = FileSource.getDefaultLocalSource(ApplicationCore.getApplication())
            .getFile(path);
        
        if(key.equalsIgnoreCase(REQUEST_GET_NAME)) {
            response.put(KEY_NAME, file.getName());
        }
          
        if(key.equalsIgnoreCase(REQUEST_LIST_FILES)) {
            File[] subFiles = file.listFiles();
            if (subFiles == null) {
                response.put(KEY_LIST_FILES, JSONObject.NULL);
            } else {
                JSONArray listFiles = new JSONArray();
                for (File subFile : subFiles) {
                    listFiles.put(subFile.getPath());
                }

                response.put(KEY_LIST_FILES, listFiles);
            }
        }
        
        if(key.equalsIgnoreCase(REQUEST_IS_DIRECTORY)) {
            response.put(KEY_IS_DIRECTORY, file.isDirectory());
        }
           
        if(key.equalsIgnoreCase(REQUEST_IS_FILE)) {
            response.put(KEY_IS_FILE, file.isFile());
        }
          
        if(key.equalsIgnoreCase(REQUEST_GET_PARENT_FILE)) {
            response.put(KEY_GET_PARENT_FILE, file.getParentFile().getPath());
        }
        
        if(key.equalsIgnoreCase(REQUEST_GET_MIME_TYPE)) {
            response.put(KEY_MIME_TYPE, file.getMimeType());
        }
        
        if(key.equalsIgnoreCase(REQUEST_GET_LENGTH)) {
        	response.put(KEY_LENGTH, file.length());
        }
        
        if(key.equalsIgnoreCase(REQUEST_GET_LAST_MODIFIED)) {
            response.put(KEY_LAST_MODIFIED, file.lastModified());
        }
        
        if(key.equalsIgnoreCase(REQUEST_MAKE_DIRECTORIES)) {
            response.put(KEY_MAKE_DIRECTORIES_SUCCESS, file.makeDirs());
        }
    }

    private Response getFile(IHTTPSession session) throws IOException {
        File file = LocalFileSource.getDefaultLocalSource(ApplicationCore.getApplication())
            .getFile(session.getUri());
        
        if(session.getHeaders().get("range") != null) {
            return getPartialContent(file,session);
        }
        
        Response response = newFixedLengthResponse(Response.Status.OK,
             file.getMimeType(), new FileInputStream(file.getPath()), file.length());
        response.addHeader("Accept-Ranges","bytes");
        
        return response;
    }

    private Response getPartialContent(File file, IHTTPSession session) throws IOException {
        InputStream stream = new FileInputStream(file.getPath());
      
        long rangeOffset = findRangeOffset(session.getHeaders().get("range"));
        long length = file.length();
        
        stream.skip(rangeOffset);
        
        Response response = newChunkedResponse(Response.Status.PARTIAL_CONTENT, file.getMimeType(), stream);
        response.addHeader("Accept-Ranges","bytes");
        response.addHeader("Content-Range", "bytes " + rangeOffset + "-" + (length) + "/" + length);     
                  
        return response;
    }
    
    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("[ ]?bytes=(\\d*)-");
        
    private long findRangeOffset(String request) {
        if(request == null) return -1;
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(request);
        if (matcher.find()) {
            String rangeValue = matcher.group(1);
            return Long.parseLong(rangeValue);
        }
        return -1;
    }
    
    @Override
    public void onRegistrationFailed(NsdServiceInfo info, int code) {
        Log.err.log(TAG, "Failed to register server with error code : " + code);
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo info, int code) {
        Log.err.log(TAG, "Failed to unregister server with error code : " + code);
    }

    @Override
    public void onServiceRegistered(NsdServiceInfo info) {
        Log.debug.log(TAG, "Sucess registering service with name " + info.getServiceName());
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo info) {
        Log.debug.log(TAG, "Sucess unregistering service");
    }

}
