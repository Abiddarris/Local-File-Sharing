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
      return null;
    }
    
}
