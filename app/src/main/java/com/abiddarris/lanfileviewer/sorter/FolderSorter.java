package com.abiddarris.lanfileviewer.sorter;

import com.abiddarris.lanfileviewer.file.File;

public class FolderSorter extends ManipulationSorter {
    
    @Override
    public int compare(File first, File second) {
        int folderOrFile = Boolean.compare(!first.isDirectory(), !second.isDirectory());
        if(folderOrFile != 0) return folderOrFile;
        
        return getSorter().compare(first, second);
    }
    
}
