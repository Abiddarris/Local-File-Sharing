package com.abiddarris.lanfileviewer.file;

import android.content.Context;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.PoolManager;
import com.gretta.util.log.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class FileSource extends PoolManager<String, File>{
    
    private static LocalFileSource localFileSource;
    public static final String TAG = Log.getTag(FileSource.class);
     
    private ExecutorService executor = Executors.newFixedThreadPool(16);
    private Context context;
    private RootFile root;
    private SecurityManager securityManager = new SecurityManager();
    
    public FileSource(Context context) {
        this.context = context;
        
        root = new RootFile(this); 
        registerToCache(root);
        
        setOneValueOnly("");
       // setSaveStackTrace(true);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new BaseRunnable((c) -> {
            Log.debug.log(TAG, toString() + " has " + getActiveObjects(new File[0]).length + " active objects");
        }), 0, 1, TimeUnit.MINUTES);
    }
    
    private String[] splitParentAndName(String path) {
        path = validatePath(path);
        if(path.equalsIgnoreCase("")) return new String[] {"", ""};
        int pathDivider = path.lastIndexOf("/");
        String name = path.substring(pathDivider + 1);
        String parent = path.substring(0,pathDivider);
        return new String[] {parent,name};
    }
    
    protected final String getName(String path) {
        return splitParentAndName(path)[1];
    }
    
    protected final String validatePath(String path) {
        if(!path.startsWith("/")) path = "/" + path;
        if(path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }
    
    protected abstract File newFile(String parent, String name);
    
    public final RootFile getRoot() {
        return root;
    }
    
    public File getFile(String path) {
        path = validatePath(path);
        return get(path);
    }
    
    @Override
    protected File create(String path) {
        if(path.equals("")) {
            throw new FileOperationException("Trapped in infinite loop! make sure to register root to cache!");
        }
        
        String[] parentAndName = splitParentAndName(path);
        
        return newFile(parentAndName[0], parentAndName[1]);
    }
    
    protected void registerToCache(File file) {
        registerToCache(file.getPath(), file);
    }
    
    protected void registerToRoot(File file) {
        getRoot()
            .addRoots(file);
        setOneValueOnly(file.getPath());
        registerToCache(file);
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
