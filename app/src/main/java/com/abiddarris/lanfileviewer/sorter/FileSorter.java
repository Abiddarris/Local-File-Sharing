package com.abiddarris.lanfileviewer.sorter;

import com.abiddarris.common.utils.sorts.AscendingSorter;
import com.abiddarris.common.utils.sorts.DelegateSorter;
import com.abiddarris.common.utils.sorts.DescendingSorter;
import com.abiddarris.common.utils.sorts.Sorter;
import com.abiddarris.lanfileviewer.file.File;

public abstract class FileSorter implements Sorter<File> {
    
    public static final int ASCENDING = 0;
    public static final int DESCENDING = 1;
    public static final int NAME = 2;
    public static final int DATE = 4;
    public static final int TYPE = 8;
    public static final int SIZE = 16;
    
    public static Sorter<File> createSorter(int flags) {
    	DelegateSorter sorter = (flags & DESCENDING) != 0 ? new DescendingSorter() : new AscendingSorter();
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
