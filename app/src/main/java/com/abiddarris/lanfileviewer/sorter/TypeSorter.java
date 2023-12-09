package com.abiddarris.lanfileviewer.sorter;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;

public class TypeSorter extends FileSorter {
    
    private NameSorter nameSorter = new NameSorter();
    
    @Override
    public int compare(File first, File second) {
        int typeResult = Files.getExtension(first).toLowerCase()
            .compareTo(Files.getExtension(second).toLowerCase());
        
        if(typeResult != 0) return typeResult;
        
        return nameSorter.compare(first,second);
    }
    
}
