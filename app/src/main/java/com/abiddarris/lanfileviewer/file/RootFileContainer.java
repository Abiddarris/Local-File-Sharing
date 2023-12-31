package com.abiddarris.lanfileviewer.file;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class RootFileContainer implements File {

    private FileSource source;
    private List<RootFile> roots = new ArrayList<>();

    public RootFileContainer(FileSource source) {
        this.source = source;
    }
    
    @Override
    public void updateData(Callback callback) {
        callback.onDataUpdated(null);
    }
    
    @Override
    public void updateDataSync() throws Exception {
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
    public File getParentFile() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public File[] listFiles() {
        return roots.toArray(new File[0]);
    }

    @Override
    public String getPath() {
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
    public boolean createNewFile() {
        return false;
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
    public FileSource getSource() {
        return source;
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
    public Progress copy(File dest) {
        Progress progress = new Progress(0);
        progress.setCompleted(true);
        progress.setException(new IOException("Cannot copy this file."));
        
        return progress;
    }
    
    @Override
    public boolean rename(String newName) {
        return false;
    }
    
    public void addRoots(RootFile rootFile) {
        roots.add(rootFile);
    }
    
}
