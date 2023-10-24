package com.abiddarris.lanfileviewer.sorter;

import com.abiddarris.lanfileviewer.file.File;

public class SizeSorter extends NameSorter {
    
    @Override
    public int compare(File first, File second) {
        if(first.isDirectory() && second.isDirectory()) {
            return super.compare(first, second);
        }
        
        if(first.isDirectory() && second.isFile()) {
            return -1;
        }
        
        if(first.isFile() && second.isDirectory()) {
            return 1;
        }
        return Long.compare(first.length(), second.length());
    }
    
}
