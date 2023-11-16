package com.abiddarris.lanfileviewer.file.sharing;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.RootFile;

public class NetworkRootFile extends NetworkFile implements RootFile {
    
    private File parent;
    
    NetworkRootFile(NetworkFileSource source, File parent, String path) {
        super(source,path);
        
        this.parent = parent;
    }
    
    @Override
    public File getParentFile() {
        return parent;
    }
    
    
}
