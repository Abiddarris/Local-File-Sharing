package com.abiddarris.lanfileviewer.file;

import android.content.Context;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.gretta.util.log.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class FileSource {
    
    private static LocalFileSource localFileSource;
    public static final String TAG = Log.getTag(FileSource.class);
     
    private ExecutorService executor = Executors.newFixedThreadPool(16);
    private Context context;
    private Map<String,File> cache = new HashMap<>();
    private SecurityManager securityManager = new SecurityManager();
    
    public FileSource(Context context) {
        this.context = context;
    }
    
    public abstract RootFile getRoot();
    
    protected abstract File newFile(File parent, String name);
    
    public File getFile(String path) {
        if(!path.startsWith("/")) path = "/" + path;
        if(path.endsWith("/")) path = path.substring(0, path.length() - 1);
        
        File file = cache.get(path);
        if(file != null) return file;
        
        int pathDivider = path.lastIndexOf("/");
        String name = path.substring(pathDivider + 1);
        
        File parent = getFile(path.substring(0,pathDivider));
        
        file = newFile(parent, name);
        
        registerToCache(file);
        
        return file;
    }
    
    public void registerToCache(File file) {
        cache.put(file.getPath(),file);
    }
    
    public Future runOnBackground(BaseRunnable runnable) {
        return executor.submit(runnable);
    }

    public SecurityManager getSecurityManager() {
        return this.securityManager;
    }
    
    public void setSecurityManager(SecurityManager securityManager) {
        this.securityManager = securityManager;
    }
    
    public Context getContext() {
        return context;
    }
    
    public static LocalFileSource getDefaultLocalSource(Context context) {
    	if(localFileSource == null) {
            localFileSource = new LocalFileSource(context);
        }
        return localFileSource;
    }
    
    public static File createFile(Context context, java.io.File file) {
        return getDefaultLocalSource(context)
            .getFile(file.getPath());
    }
    
    public static File createFile(Context context, File parent, String name) {
        return getDefaultLocalSource(context)
            .getFile(parent.getPath() + "/" + name);
    }
    
    public static File getCacheDirectory(Context context) {
        return getDefaultLocalSource(context)
            .getFile(context.getCacheDir().getPath());
    }
}
