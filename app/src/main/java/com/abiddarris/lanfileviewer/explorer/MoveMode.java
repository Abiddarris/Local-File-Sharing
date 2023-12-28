package com.abiddarris.lanfileviewer.explorer;

import com.abiddarris.lanfileviewer.R;
import java.util.Set;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;

public class MoveMode extends CopyMode {
    
    public MoveMode(Explorer explorer) {
        super(explorer);
    }
    
    @Override
    protected int getActionText() {
        return R.string.move_to_here;
    }
    
    @Override
    protected ActionRunnable getRunnable(Set<File> items, File dest) {
        return super.getRunnable(items, dest);
    }
    
}
