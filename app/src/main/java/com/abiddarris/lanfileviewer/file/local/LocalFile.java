package com.abiddarris.lanfileviewer.file.local;

import android.net.Uri;
import android.webkit.MimeTypeMap;
import com.abiddarris.lanfileviewer.file.File;
import java.io.IOException;

public class LocalFile implements File {

    private java.io.File file;
    private File parent;
    private LocalFileSource source;

    protected LocalFile(LocalFileSource source, File parent, java.io.File file) {
        this.source = source;
        this.parent = parent;
        this.file = file;
    }

    @Override
    public void updateData(Callback callback) {
        callback.onDataUpdated();
    }

    @Override
    public boolean isDirectory() {
        source.getSecurityManager()
            .checkRead(this);
        
        return file.isDirectory();
    }

    @Override
    public boolean isFile() {
        source.getSecurityManager()
            .checkRead(this);
        
        return file.isFile();
    }

    @Override
    public File getParentFile() {
        return parent;
    }

    @Override
    public String getName() {
        source.getSecurityManager()
            .checkRead(this);
        
        return file.getName();
    }

    @Override
    public File[] listFiles() {
        source.getSecurityManager()
            .checkRead(this);
        
        java.io.File[] javaFiles = file.listFiles();
        if(javaFiles == null) return null;
        
        File[] files = new File[javaFiles.length];
        for(int i = 0; i < files.length; ++i) {
        	files[i] = source.getFile(javaFiles[i]
                .getPath());
        }
        
        return files;
    }
    
    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public Uri toUri() {
        source.getSecurityManager()
            .checkRead(this);
        
        return Uri.fromFile(file);
    }

    @Override
    public String getMimeType() {
        Uri uri = toUri();
        
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null) {
            type = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension.toLowerCase());
        }
        
        return type;
    }
    
    @Override
    public long length() {
        source.getSecurityManager()
            .checkRead(this);
        
        return file.length();
    }
    
    @Override
    public long lastModified() {
        source.getSecurityManager()
            .checkRead(this);
        
        return file.lastModified();
    }
    
    @Override
    public boolean createNewFile() throws IOException {
        source.getSecurityManager()
            .checkWrite(this);
        
        return file.createNewFile();
    }
    
    
}
