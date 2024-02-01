package com.abiddarris.lanfileviewer.file.local;

import android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Requests;
import com.abiddarris.lanfileviewer.file.RootFile;
import com.gretta.util.log.Log;
import java.util.Arrays;

public class LocalFileSource extends FileSource {

    private DocumentFile sdCardDocumentFile;
    private RootFile root;
    private File sdCardStorage;
    
    public static final String SD_CARD_URI_KEY = "sdCardUriKey";
    public static final String TAG = Log.getTag(LocalFileSource.class);
    
    public LocalFileSource(Context context, java.io.File[] files) {
        super(context);
        
        root = new RootFile(this);
        
        for(java.io.File file : files) {
            File rootChild = new LocalFile(this, root, file);
            rootChild.updateDataSync(Requests.REQUEST_ABSOLUTE_PATH);
            Log.debug.log(TAG, String.format("javaFile : %s, file path : %s, abs path: %s", file.getAbsolutePath(), rootChild.getPath(), rootChild.getAbsolutePath()));
            root.addRoots(rootChild);
            
            registerToCache(rootChild);
        }
        
        initSdCardSupport(context);
        
        registerToCache(root);
    }
    
    private void initSdCardSupport(Context context) {
        String sdCardPath = getSDCardPath(context);
        
        Log.debug.log(TAG, sdCardPath);
        if(sdCardPath != null) {
            java.io.File file = new java.io.File(sdCardPath);
            
            if(!file.canWrite()) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                Uri uri = Uri.parse(preferences.getString(SD_CARD_URI_KEY, null));
        
                sdCardDocumentFile = DocumentFile.fromTreeUri(context, uri);
            }
            
            sdCardStorage = new LocalFile(this, root, file);
        }
    }
    
    public LocalFileSource(Context context) {
        super(context);
        
        root = new RootFile(this);
        
        File internalStorage = new LocalFile(this, root,
            Environment.getExternalStorageDirectory());
        
        root.addRoots(internalStorage);
        
        initSdCardSupport(context);
        
        if(sdCardStorage != null) {
            registerToCache(sdCardStorage);
            root.addRoots(sdCardStorage);
        } 
        registerToCache(root);
        registerToCache(internalStorage);
    }

    @Override
    public RootFile getRoot() {
        return root;
    }
    
    protected DocumentFile findDocumentFile(File file) {
        String filePath = file.getAbsolutePath();
        
    	if(sdCardStorage == null || !filePath.toLowerCase()
            .startsWith(sdCardStorage.getAbsolutePath().toLowerCase())) {
            Log.debug.log(TAG, "Returning from findDocumentFile(File)");
            return null;
        }
        
        if(filePath.length() == sdCardStorage.getAbsolutePath().length()) {
            return sdCardDocumentFile;
        }
        
        String[] paths = filePath.substring(sdCardStorage.getAbsolutePath().length() + 1)
            .split("/");
        
        Log.debug.log(TAG, Arrays.toString(paths));
        
        return findDocumentFile(sdCardDocumentFile, paths, 0);
    }
    
    private DocumentFile findDocumentFile(DocumentFile parent, String[] paths, int pathIndex) {
    	DocumentFile file = parent.findFile(paths[pathIndex]);
        pathIndex++;
        
        Log.debug.log(TAG, "File : " + file);
        Log.debug.log(TAG, "index : " + pathIndex);
      
        if(paths.length == pathIndex || file == null) {
            return file;
        } 
        return findDocumentFile(file, paths, pathIndex);
    }

    @Override
    protected File newFile(String parent, String name) {
        return new LocalFile(this, parent, name);
    }
    
    private static String getSDCardPath(Context context) {
        java.io.File internalStorage = Environment.getExternalStorageDirectory();
        
        java.io.File[] externalCacheDirs = context.getExternalCacheDirs();
        for (java.io.File file : externalCacheDirs) {
            Log.debug.log(TAG, file);
            
            if(!file.getPath().startsWith(internalStorage.getPath())) {
                // Path is in format /storage.../Android....
                // Get everything before /Android
                return file.getPath().split("/Android")[0];
            }
        }
        return null;
    }
    
    public static boolean hasSDCard(Context context) {
    	return getSDCardPath(context) != null;
    }
    
    public static boolean canWriteToSDCard(Context context) {
        String path = getSDCardPath(context);
        if(path == null) return false;
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String uriPath = preferences.getString(SD_CARD_URI_KEY, null);
        if(uriPath != null) {
            return true;
        }
        
    	return new java.io.File(path)
            .canWrite();
    }
    
    public static void setSDCardFallback(Context context, Uri sdCardRoot) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        preferences.edit()
            .putString(SD_CARD_URI_KEY, sdCardRoot.toString())
            .commit();
    }
  
    public static String getInternalStoragePath() {
        return Environment.getExternalStorageDirectory()
            .getPath();
    }
    
    public static String getExternalStoragePath(Context context) {
    	return getSDCardPath(context);
    }
}
