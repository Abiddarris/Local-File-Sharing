package com.abiddarris.lanfileviewer.sorter;

import com.abiddarris.lanfileviewer.file.File;

public class LastModifiedSorter extends FileSorter {

    @Override
    public int compare(File first, File second) {
        return Long.compare(first.lastModified(), second.lastModified());
    }
}
