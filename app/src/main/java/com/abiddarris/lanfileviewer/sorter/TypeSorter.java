package com.abiddarris.lanfileviewer.sorter;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;

public class TypeSorter extends FileSorter {
    
    @Override
    public int compare(File first, File second) {
        return Files.getExtension(first).toLowerCase()
            .compareTo(Files.getExtension(second).toLowerCase());
    }
    
}
