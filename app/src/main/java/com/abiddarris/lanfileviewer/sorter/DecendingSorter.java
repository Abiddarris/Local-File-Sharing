package com.abiddarris.lanfileviewer.sorter;

import com.abiddarris.lanfileviewer.file.File;

public class DecendingSorter extends ManipulationSorter {

    @Override
    public int compare(File first, File second) {
        return getSorter().compare(second,first);
    }
}
