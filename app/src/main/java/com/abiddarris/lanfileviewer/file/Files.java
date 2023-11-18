package com.abiddarris.lanfileviewer.file;
import java.util.List;

public class Files {
    
    public static void getFilesTree(List<File> files, File parent) {
        files.add(parent);
        if(parent.isFile()) {
            return;
        }
        
        File[] children = parent.listFiles();
        if(children == null) return;
        
        for(File file : children) {
            getFilesTree(files,file);
        }
    }
    
}
