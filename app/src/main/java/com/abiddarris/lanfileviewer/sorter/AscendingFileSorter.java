package com.abiddarris.lanfileviewer.sorter;

import com.abiddarris.lanfileviewer.file.File;

public class AscendingFileSorter extends ManipulationSorter {

    @Override
    public int compare(File first, File second) {
        return getSorter().compare(first, second);
    }

}
