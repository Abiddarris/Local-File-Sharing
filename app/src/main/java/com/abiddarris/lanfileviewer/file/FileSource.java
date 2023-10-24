package com.abiddarris.lanfileviewer.file;

import android.content.Context;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.gretta.util.log.Log;
import java.util.HashMap;
import java.util.Map;

public abstract class FileSource {
    
    private Map<String,File> cache = new HashMap<>();
    
    private static LocalFileSource localFileSource;
    public static final String TAG = Log.getTag(FileSource.class);
    
    public abstract File getRoot();
    
    protected abstract File newFile(File parent, String path);
    
    public File getFile(String path) {
        File file = cache.get(path);
        if(file != null) return file;
        
        int pathDivider = path.lastIndexOf("/");
        
        File parent = getFile(path.substring(0,pathDivider));
        file = newFile(parent, path);
        
        registerToCache(file);
        
        return file;
    }
    
    public void registerToCache(File file) {
        cache.put(file.getPath(),file);
    }
    
    public static LocalFileSource getDefaultLocalSource(Context context) {
    	if(localFileSource == null) {
            localFileSource = new LocalFileSource(context);
        }
        return localFileSource;
    }
}
