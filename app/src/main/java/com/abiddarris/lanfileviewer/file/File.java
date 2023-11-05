package com.abiddarris.lanfileviewer.file;

import android.net.Uri;
import org.json.JSONObject;

public interface File {
    
    public abstract void updateData(Callback callback);
    
    public abstract boolean isDirectory();
    
    public abstract boolean isFile();
    
    public abstract File getParentFile();
    
    public abstract String getName();
    
    public abstract File[] listFiles();
    
    public abstract String getPath();
    
    public abstract Uri toUri();
    
    public abstract String getMimeType();
    
    public abstract long length();
    
    public abstract long lastModified();
    
    public abstract boolean createNewFile();
    
    public static interface Callback {
        void onDataUpdated();
    }
    
}
