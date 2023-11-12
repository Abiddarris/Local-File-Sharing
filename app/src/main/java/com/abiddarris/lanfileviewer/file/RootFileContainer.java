package com.abiddarris.lanfileviewer.file;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class RootFileContainer implements File {

    private List<RootFile> roots = new ArrayList<>();

    @Override
    public void updateData(Callback callback) {
        callback.onDataUpdated();
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

    public void addRoots(RootFile rootFile) {
        roots.add(rootFile);
    }
    
}
