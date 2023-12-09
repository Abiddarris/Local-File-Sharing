package com.abiddarris.lanfileviewer.sorter;

import android.webkit.DateSorter;
import com.abiddarris.lanfileviewer.file.File;
import java.util.Comparator;

public abstract class FileSorter implements Comparator<File> {
    
    public static final int ASCENDING = 0;
    public static final int DECENDING = 1;
    public static final int NAME = 2;
    public static final int DATE = 4;
    public static final int TYPE = 8;
    public static final int SIZE = 16;
    
    public static FileSorter createSorter(int flags) {
    	ManipulationSorter sorter = (flags & DECENDING) != 0 ? new DecendingSorter() : new AscendingSorter();
        FileSorter sortBy = null;
        
        if((flags & NAME) != 0 && sortBy == null) sortBy = new NameSorter();
        if((flags & DATE) != 0 && sortBy == null) sortBy = new LastModifiedSorter();
        if((flags & TYPE) != 0 && sortBy == null) sortBy = new TypeSorter();
        if((flags & SIZE) != 0 && sortBy == null) sortBy = new SizeSorter();
        
        if(sortBy == null) throw new IllegalArgumentException("flags does not contain sortby bit");
        
        sorter.setSorter(sortBy);
        
        FolderSorter folderSorter = new FolderSorter();
        folderSorter.setSorter(sorter);
        
        return folderSorter;
    }
    
}
