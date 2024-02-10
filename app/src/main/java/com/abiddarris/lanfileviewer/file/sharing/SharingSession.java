package com.abiddarris.lanfileviewer.file.sharing;

import static com.abiddarris.lanfileviewer.file.Requests.*;
import static com.abiddarris.lanfileviewer.file.sharing.JSONRequest.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Base64;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.abiddarris.lanfileviewer.utils.Thumbnails;
import com.abiddarris.lanfileviewer.utils.Timer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gretta.util.log.Log;
import fi.iki.elonen.NanoHTTPD;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class SharingSession extends NanoHTTPD implements RegistrationListener {
    
    private Context context;
    private FileSource source;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private Map<Integer, File.Progress> progresses = new HashMap<>();
    private NsdManager nsdManager;
    
    private static final String TAG = Log.getTag(SharingSession
        .class);
    
    public SharingSession(Context context, FileSource source) {
        super(0);
        this.context = context;
        this.source = source;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }
    
    public void start(String name) throws IOException {
        start(SOCKET_READ_TIMEOUT);
        
        NsdServiceInfo info = new NsdServiceInfo();
        info.setServiceName(name + "_FILEV");
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
            
            InputStream inputStream = null;
            long size = -1;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(baos));
                oos.writeObject(e);
                oos.flush();
                oos.close();
                
                size = baos.size();
                inputStream = new ByteArrayInputStream(baos.toByteArray());
            } catch (IOException e1) {
                Log.err.log(TAG, e1);
            }
            
            if(inputStream == null) return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Failed to sent error message");
            
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/octet-stream", inputStream, size);
        }
    }

    private Response handlePost(IHTTPSession session) throws Exception {
        String uri = session.getUri();
        if(uri.startsWith("/fetch")) {
            return handleFetch(session);
        } else if(uri.equalsIgnoreCase("/upload")) {
            return handleUpload(session);
        }
        return null;
    }
    
    private Response handleUpload(IHTTPSession session) throws Exception {
    	Map<String,String> body = new HashMap<>();
        session.parseBody(body);
        
        Map<String,String> params = session.getParms();
        File dest = null;
        File tempFile = null;
        
        try {
            dest = source.getFile(params.get("path"));
            tempFile = source.getFile(body.get("stream"));
            
            BufferedInputStream is = new BufferedInputStream(tempFile.newInputStream());
            BufferedOutputStream os = new BufferedOutputStream(dest.newOutputStream());
            byte[] buf = new byte[8 * 1024];
            int len;
            while((len = is.read(buf)) != -1) {
                os.write(buf,0,len);
            }
            os.flush();
            os.close();
            is.close();
        
            return newFixedLengthResponse("success");
        } finally {
            if(dest != null)
                FileSource.freeFiles(dest);
            if(tempFile != null) {
                FileSource.freeFiles(tempFile);
            }
        }
    }

    private Response handleFetch(IHTTPSession session) throws Exception {
        Map<String,String> body = new HashMap<>();
        session.parseBody(body);
        
        String requestString = body.get("postData");
        JSONObject request = new JSONObject(requestString);
        JSONObject response = new JSONObject();
        
        JSONArray requestKeys = request.getJSONArray(KEY_REQUEST);
        String path = request.optString(KEY_PATH);
        for(int i = 0; i < requestKeys.length(); ++i) {
        	String key = requestKeys.getString(i);
            if(path != null) {
                fetchFileRelated(request,response,key,path);
            }
            
            fetchOthersRequest(request,response,key,path);
        }
        
        return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
    }
    
    private void fetchOthersRequest(JSONObject request, JSONObject response, String key, String path) throws Exception {
        if(key.equals(REQUEST_GET_TOP_DIRECTORY_FILES)) {
        	JSONArray topDirectoryFiles = new JSONArray();
            File root = source.getRoot();
            for(File subroot : root.listFiles()) {
                topDirectoryFiles.put(subroot.getPath());
            }
 
            response.put(KEY_TOP_DIRECTORY_FILES,topDirectoryFiles);
        } else if(key.equals(REQUEST_PROGRESS)) {
            int id = request.optInt(KEY_PROGRESS_ID);
            File.Progress progress = progresses.get(id);
            boolean completed = progress.isCompleted();
            
            response.put(KEY_COMPLETED, completed);
            response.put(KEY_PROGRESS, progress.getCurrentProgress());
            response.put(KEY_LENGTH, progress.getSize());
            
            if(completed && progress.getException() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(baos));
                stream.writeObject(progress.getException());
                stream.flush();
                stream.close();
                
                byte[] data = Base64.encode(baos.toByteArray(),Base64.DEFAULT);
                response.put(KEY_EXCEPTION, new String(data));
            }
        } else if(key.equalsIgnoreCase(REQUEST_CANCEL_PROGRESS)) {
            int id = request.optInt(KEY_PROGRESS_ID);
            File.Progress progress = progresses.get(id);
            progress.setCancel(true);
        } else if(key.equalsIgnoreCase(REQUEST_REMOVE_PROGRESS)) {
           int id = request.optInt(KEY_PROGRESS_ID);
           progresses.remove(id);
        }
    }
    
    private void fetchFileRelated(JSONObject request, JSONObject response, String key, String path) throws Exception {
        File file = null;
        try {
            file = source.getFile(path);
            fetchFileRelated(request,response,key,file);
        } finally {
            if(file != null && !(key.equalsIgnoreCase(REQUEST_COPY) || key.equalsIgnoreCase(REQUEST_MOVE))) {
                FileSource.freeFiles(file);
            }
        }
        
    }

    private void fetchFileRelated(JSONObject request, JSONObject response, String key, final File file) throws Exception {
        file.updateDataSync(key);
        
        if(key.equalsIgnoreCase(REQUEST_LIST)) {
            String[] names = file.list();
            if (names == null) {
                response.put(KEY_LIST, JSONObject.NULL);
            } else {
                JSONArray listFiles = new JSONArray();
                for (String name : names) {
                    listFiles.put(name);
                }
                
                response.put(KEY_LIST, listFiles);
            }
        } else if(key.equalsIgnoreCase(REQUEST_IS_DIRECTORY)) {
            response.put(KEY_IS_DIRECTORY, file.isDirectory());
        } else if(key.equalsIgnoreCase(REQUEST_IS_FILE)) {
            response.put(KEY_IS_FILE, file.isFile());
        } else if(key.equalsIgnoreCase(REQUEST_GET_MIME_TYPE)) {
            response.put(KEY_MIME_TYPE, file.getMimeType());
        } else if(key.equalsIgnoreCase(REQUEST_GET_LENGTH)) {
        	response.put(KEY_LENGTH, file.length());
        } else if(key.equalsIgnoreCase(REQUEST_GET_LAST_MODIFIED)) {
            response.put(KEY_LAST_MODIFIED, file.lastModified());
        } else if(key.equalsIgnoreCase(REQUEST_MAKE_DIRECTORIES)) {
            response.put(KEY_MAKE_DIRECTORIES_SUCCESS, file.makeDirs());
        } else if(key.equalsIgnoreCase(REQUEST_EXISTS)) {
            response.put(KEY_EXISTS, file.exists());
        } else if(key.equalsIgnoreCase(REQUEST_COPY)) {
            final File dest = source.getFile(
                request.getString(KEY_DEST));
            dest.updateDataSync();
            
            File.Progress progress = file.copy(dest, (p) -> {
                FileSource.freeFiles(file, dest);
            });
            progresses.put(progress.hashCode(), progress);
            
            response.put(KEY_PROGRESS_ID, progress.hashCode());
        } else if(key.equalsIgnoreCase(REQUEST_RENAME)) {
            String newName = request.getString(KEY_NEW_NAME);
            boolean sucess = file.rename(newName);
            response.put(KEY_SUCESS, sucess);
        } else if(key.equalsIgnoreCase(REQUEST_DELETE)) {
            boolean success = file.delete();
            response.put(KEY_SUCESS, success);
        } else if(key.equalsIgnoreCase(REQUEST_MOVE)) {
            File dest = source.getFile(
                    request.getString(KEY_DEST));
            dest.updateDataSync();
            
            File.Progress progress = file.move(dest, (p) -> {
                FileSource.freeFiles(file, dest);
            });
            progresses.put(progress.hashCode(), progress);
            
            response.put(KEY_PROGRESS_ID, progress.hashCode());
        } else if(key.equalsIgnoreCase(REQUEST_ABSOLUTE_PATH)) {
            response.put(KEY_ABSOLUTE_PATH, file.getAbsolutePath());
        } else if(key.equalsIgnoreCase(REQUEST_GET_FILES_TREE)) {
            List<File> trees = file.getFilesTree();
            JSONArray files = new JSONArray();
            for(File tree : trees) {
                files.put(tree.getPath());
                FileSource.freeFiles(tree);
            }
            
            response.put(KEY_FILES_TREE, files);
        } else if(key.equalsIgnoreCase(REQUEST_GET_FILES_TREE_SIZE)) {
            response.put(KEY_FILES_TREE_SIZE, file.getFilesTreeSize());
        }
    }

    private Response getFile(IHTTPSession session) throws Exception {
        File file = null;
        try {
            file = source.getFile(session.getUri());
            file.updateDataSync(REQUEST_ABSOLUTE_PATH, 
                REQUEST_GET_LENGTH,REQUEST_GET_MIME_TYPE);
        
            String type = session.getParms().get("type");
            if(type != null && type.equalsIgnoreCase("thumbnail")) {
                Timer timer = new Timer();
            
                java.io.File f = Thumbnails.getThumbnail(getContext(), new java.io.File(file.getAbsolutePath()));
            
                Log.debug.log(TAG, "file : " + file.getPath() + ", originalSize : " + file.length() + ", time : " + timer.reset() + " ms, thumb : " + f + ", size :" + (f != null ? f.length() : 0));
     
                if(f == null) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found");
                }
                
                FileSource.freeFiles(file);
                
                file = source.getFile(f.getPath());
                file.updateDataSync(REQUEST_GET_LENGTH, REQUEST_GET_MIME_TYPE);
            }
        
            if(session.getHeaders().get("range") != null) {
                return getPartialContent(file,session);
            }
        
            Response response = newFixedLengthResponse(Response.Status.OK,
                file.getMimeType(), file.newInputStream(), file.length());
            response.addHeader("Accept-Ranges","bytes");
        
            return response;
        } finally {
            if(file != null) {
                FileSource.freeFiles(file);
            }
        }
    }

    private Response getPartialContent(File file, IHTTPSession session) throws IOException {
        InputStream stream = file.newInputStream();
      
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
    
    private static class LoadTarget extends CustomTarget<java.io.File> {

        private CountDownLatch lock = new CountDownLatch(1);
        private volatile java.io.File thumbnail;        

        @Override
        public void onLoadCleared(Drawable arg0) {}

        @Override
        public void onResourceReady(java.io.File thumbnail, Transition<? super java.io.File> arg1) {
            this.thumbnail = thumbnail;
            Log.debug.log("ThumbnailLoader", "success ");
            lock.countDown();
        }

        @Override
        public void onLoadFailed(Drawable drawable) {
                        Log.debug.log("ThumbnailLoader", "failed ");
            lock.countDown();
        }
    }
}
