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
import com.abiddarris.lanfileviewer.file.RootFile;
import com.abiddarris.lanfileviewer.file.RootFileContainer;
import com.gretta.util.log.Log;
import java.util.Arrays;

public class LocalFileSource extends FileSource {

    private DocumentFile sdCardDocumentFile;
    private RootFileContainer root;
    private RootFile sdCardStorage;
    
    public static final String SD_CARD_URI_KEY = "sdCardUriKey";
    public static final String TAG = Log.getTag(LocalFileSource.class);
    
    public LocalFileSource(Context context) {
        super(context);
        
        root = new RootFileContainer(this);
        
        RootFile internalStorage = new LocalRootFile(this, root,
            Environment.getExternalStorageDirectory());
        root.addRoots(internalStorage);
        
        String sdCardPath = getSDCardPath(context);
        
        Log.debug.log(TAG, sdCardPath);
        
        if(sdCardPath != null) {
            java.io.File file = new java.io.File(sdCardPath);
            
            if(!file.canWrite()) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                Uri uri = Uri.parse(preferences.getString(SD_CARD_URI_KEY, null));
        
                sdCardDocumentFile = DocumentFile.fromTreeUri(context, uri);
            }
            
            sdCardStorage = new LocalRootFile(this, root, file);
            root.addRoots(sdCardStorage);
            
            registerToCache(sdCardStorage);
        }
        
        registerToCache(root);
        registerToCache(internalStorage);
    }

    @Override
    public RootFileContainer getRoot() {
        return root;
    }
    
    protected DocumentFile findDocumentFile(File file) {
        String filePath = file.getPath();
    	if(sdCardStorage == null || !filePath.toLowerCase()
            .startsWith(sdCardStorage.getPath().toLowerCase())) {
            Log.debug.log(TAG, "Returning from findDocumentFile(File)");
            return null;
        }
        
        if(filePath.length() == sdCardStorage.getPath().length()) {
            return sdCardDocumentFile;
        }
        
        String[] paths = filePath.substring(sdCardStorage.getPath().length() + 1)
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
    protected File newFile(File parent, String path) {
        java.io.File file = new java.io.File(path);
       
        return new LocalFile(this, parent, file);
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
  
}
