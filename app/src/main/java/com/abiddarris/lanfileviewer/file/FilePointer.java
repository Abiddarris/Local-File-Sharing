package com.abiddarris.lanfileviewer.file;

import com.gretta.util.recycler.Poolable;

public final class FilePointer extends Poolable {
    
    private FileSource source;
    private String path;
    
    FilePointer(FileSource source, String path) {
        this.source = source;
        this.path = path;
    }
    
    public File get() {
        return source.getFile(path);
    }
    
    public FileSource getSource() {
        return source;
    }
    
    public String getPath() {
        return path;
    }
    
}
