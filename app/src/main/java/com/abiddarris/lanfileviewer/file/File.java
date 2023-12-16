package com.abiddarris.lanfileviewer.file;

import android.net.Uri;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.json.JSONObject;

public interface File {
    
    public abstract void updateData(Callback callback);
    
    public abstract void updateDataSync() throws Exception;
    
    public abstract FileSource getSource();
    
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
    
    public abstract boolean createNewFile() throws IOException;
    
    public abstract boolean makeDirs();
    
    public abstract boolean exists();
    
    public abstract OutputStream newOutputStream() throws IOException;
    
    public abstract InputStream newInputStream() throws IOException;
    
    public static interface Callback {
        void onDataUpdated(Exception e);
    }
    
}
