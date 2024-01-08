package com.abiddarris.lanfileviewer.explorer;

import com.abiddarris.lanfileviewer.file.File;
import java.util.NavigableSet;

public abstract class Mode {

    private Explorer explorer;
    
    public Mode(Explorer explorer) {
        this.explorer = explorer;
    }

    public void onParentChanged(File newParent) {}
    
    public abstract void onItemClickListener(FileAdapter.ViewHolder holder, int pos);

    public abstract void onItemLongClickListener(FileAdapter.ViewHolder holder, int pos);

    public abstract void onViewBind(FileAdapter.ViewHolder holder, int pos);
    
    public boolean onBackPressed() { return false;}
    
    public void onModeSelected() {}
    
    public void onModeDeselected() {}
    
    public Explorer getExplorer() {
        return this.explorer;
    }

}
