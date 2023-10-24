package com.abiddarris.lanfileviewer.file.network;

import static com.abiddarris.lanfileviewer.file.network.JSONRequest.*;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.RootFile;
import com.abiddarris.lanfileviewer.file.RootFileContainer;
import org.json.JSONArray;
import org.json.JSONObject;

public class NetworkFileSource extends FileSource {

    private RootFileContainer root;
    private NetworkFileClient client;

    NetworkFileSource(NetworkFileClient client) throws Exception {
        this.client = client;
        
        JSONObject request = new JSONObject()
            .put(KEY_REQUEST, REQUEST_GET_TOP_DIRECTORY_FILES);
       
        client.sendRequest(request, (response) -> {
            JSONArray jsonTopDirectoryFiles = response.optJSONArray(KEY_TOP_DIRECTORY_FILES);
            root = new RootFileContainer(); 
            
            for(int i = 0; i < jsonTopDirectoryFiles.length(); ++i) {
                String path = jsonTopDirectoryFiles.optString(i);
                RootFile child = new NetworkRootFile(this, root, path);
                    
                registerToCache(child); 
                root.addRoots(child);
            }
            
            registerToCache(root);
            
            client.getConnectedCallback()
                .onConnected(this);
        });
        
    }

    @Override
    public File getRoot() {
        return root;
    }

    @Override
    protected File newFile(File parent, String path) {
        return new NetworkFile(this,path);
    }

    public NetworkFileClient getClient() {
        return this.client;
    }

}
