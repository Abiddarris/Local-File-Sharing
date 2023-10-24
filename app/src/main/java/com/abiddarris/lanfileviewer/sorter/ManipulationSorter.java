package com.abiddarris.lanfileviewer.sorter;

public abstract class ManipulationSorter extends FileSorter {

    private FileSorter sorter;

    public FileSorter getSorter() {
        return this.sorter;
    }

    public void setSorter(FileSorter sorter) {
        this.sorter = sorter;
    }
}
