package com.abiddarris.lanfileviewer.explorer;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.runnables.MoveRunnable;
import com.abiddarris.lanfileviewer.file.FilePointer;
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
    protected ActionRunnable getRunnable(File[] items, FilePointer dest) {
        return new MoveRunnable(items, dest);
    }
    
}
