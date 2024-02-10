package com.abiddarris.lanfileviewer.file;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class RootFile extends File {

    private FileSource source;
    private List<FilePointer> roots = new ArrayList<>();

    public RootFile(FileSource source) {
        super(source, null, "");
        setPath("");
    }
   
    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }
    
    @Override
    public File[] listFiles() {
        File[] files = new File[roots.size()];
        for(int i = 0; i < files.length; i++) {
            files[i] = roots.get(i)
                .get();
        }
        return files;
    }

    @Override
    public String getAbsolutePath() {
        return "";
    }

    @Override
    public Uri toUri() {
        return null;
    }

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public long lastModified() {
        return -1;
    }
  
    @Override
    public boolean makeDirs() {
        return false;
    }
    
    @Override
    public boolean exists() {
        return true;
    }
    
    @Override
    public InputStream newInputStream() throws IOException {
        throw new IOException("unable to open root file container");
    }
    
    @Override
    public OutputStream newOutputStream() throws IOException {
        throw new IOException("unable to open root file container");
    }
    
    @Override
    public Progress copy(File dest, OnOperationDoneListener listener) {
        Progress progress = new Progress(0);
        progress.setCompleted(true);
        progress.setException(new IOException("Cannot copy this file."));
        
        listener.onOperationDone(progress);
        
        return progress;
    }
    
    @Override
    public boolean rename(String newName) {
        return false;
    }
    
    @Override
    public boolean delete() {
        return false;
    }
    
    @Override
    public Progress move(File dest) {
        Progress progress = new Progress(0);
        progress.setCompleted(true);
        progress.setException(new IOException("Cannot move this file."));
        
        return progress;
    }
    
    @Override
    public void createThumbnail(ThumbnailCallback callback) {
        callback.onThumbnailCreated(null);
    }
    
    void addRoots(FilePointer pointer) {
        roots.add(pointer);
    }
}
