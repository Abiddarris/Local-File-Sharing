package com.abiddarris.lanfileviewer.file.network;

import static com.abiddarris.lanfileviewer.file.network.JSONRequest.*;

import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONRequestHandler extends RequestHandler {

    private static final int MAX_WRITE_SIZE = 1024 * 32;
    
    @Override
    public void onExecute() throws Exception {
        DataInputStream input = new DataInputStream(getInputStream());
        DataOutputStream output = new DataOutputStream(getOutputStream());
        
        while (true) {
            JSONObject response = handleRequest(input.readUTF());
            
            String[] responses = splitResponse(response.toString());
            for(String responseStr : responses) {
                output.writeUTF(responseStr);
            }
        }
    }

    private String[] splitResponse(String string) {
        List<String> responses = new ArrayList<>();
        while(string.length() > MAX_WRITE_SIZE) {
            String response = string.substring(0, MAX_WRITE_SIZE);
            responses.add(response);
            
            string = string.substring(MAX_WRITE_SIZE, string.length());
        }
        responses.add(string + "[END]");
        
        return responses.toArray(new String[0]);
    }

    private JSONObject handleRequest(final String requestString) throws Exception {
        JSONObject request = new JSONObject(requestString);
        JSONObject response = new JSONObject();
        
        response.put(KEY_ID, request.getInt(KEY_ID));
        
        int requestCode = request.getInt(KEY_REQUEST);
        String path = request.optString(KEY_PATH);
        if(path != null) {
            handleFileRelated(request,response,requestCode,path);
        }
        
        if((requestCode & REQUEST_GET_TOP_DIRECTORY_FILES) != 0) {
        	JSONArray topDirectoryFiles = new JSONArray();
            File root = LocalFileSource.getDefaultLocalSource(
                ApplicationCore.getApplication()).getRoot();
            for(File subroot : root.listFiles()) {
                topDirectoryFiles.put(subroot.getPath());
            }
 
            response.put(KEY_TOP_DIRECTORY_FILES,topDirectoryFiles);
        }
        
        return response;
    }

    private void handleFileRelated(final JSONObject request, final JSONObject response, final int requestCode, final String path) throws JSONException {
        File file = FileSource.getDefaultLocalSource(ApplicationCore.getApplication())
            .getFile(path);
        
        if ((requestCode & REQUEST_GET_NAME) != 0) {
            response.put(KEY_NAME, file.getName());
        }
          
        if ((requestCode & REQUEST_LIST_FILES) != 0) {
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
        
        if ((requestCode & REQUEST_IS_DIRECTORY) != 0) {
            response.put(KEY_IS_DIRECTORY, file.isDirectory());
        }
           
        if ((requestCode & REQUEST_IS_FILE) != 0) {
            response.put(KEY_IS_FILE, file.isFile());
        }
          
        if ((requestCode & REQUEST_GET_PARENT_FILE) != 0) {
            response.put(KEY_GET_PARENT_FILE, file.getParentFile().getPath());
        }
        
        if((requestCode & REQUEST_GET_MIME_TYPE) != 0) {
            response.put(KEY_MIME_TYPE, file.getMimeType());
        }
        
        if((requestCode & REQUEST_GET_LENGTH) != 0) {
        	response.put(KEY_LENGTH, file.length());
        }
        
        if((requestCode & REQUEST_GET_LAST_MODIFIED) != 0) {
            response.put(KEY_LAST_MODIFIED, file.lastModified());
        }
    }

}
