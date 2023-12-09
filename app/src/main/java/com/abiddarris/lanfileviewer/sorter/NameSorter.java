package com.abiddarris.lanfileviewer.sorter;

import com.abiddarris.lanfileviewer.file.File;

public class NameSorter extends FileSorter {

    @Override
    public int compare(File first, File second) {
        return first.getName().toLowerCase()
            .compareTo(second.getName().toLowerCase());
    }

}
