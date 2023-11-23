package com.abiddarris.lanfileviewer.file.local;

import android.R;
import android.content.Context;
import android.os.Environment;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.RootFile;
import com.abiddarris.lanfileviewer.file.RootFileContainer;
import com.gretta.util.log.Log;

public class LocalFileSource extends FileSource {

    private RootFileContainer root;
    
    public static final String TAG = Log.getTag(LocalFileSource.class);
    
    public LocalFileSource(Context context) {
        root = new RootFileContainer(this);
        
        RootFile internalStorage = new LocalRootFile(this, root,
            Environment.getExternalStorageDirectory());
        root.addRoots(internalStorage);
        
        String sdCardPath = null;
        java.io.File[] externalCacheDirs = context.getExternalCacheDirs();
        for (java.io.File file : externalCacheDirs) {
            Log.debug.log(TAG, file);
            Log.debug.log(TAG, internalStorage.getPath());
            if(!file.getPath().startsWith(internalStorage.getPath())) {
                // Path is in format /storage.../Android....
                // Get everything before /Android
                sdCardPath = file.getPath().split("/Android")[0];
                break;
            }
        }
        
        Log.debug.log(TAG, sdCardPath);
        
        if(sdCardPath != null) {
            RootFile sdCardStorage = new LocalRootFile(this, root, new java.io.File(sdCardPath));
            root.addRoots(sdCardStorage);
            
            registerToCache(sdCardStorage);
        }
        
        registerToCache(root);
        registerToCache(internalStorage);
    }

    @Override
    public File getRoot() {
        return root;
    }

    @Override
    protected File newFile(File parent, String path) {
         return new LocalFile(this, parent, new java.io.File(path));
    }
}
