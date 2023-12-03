package com.abiddarris.lanfileviewer.actions;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;
import com.gretta.util.log.Log;

public abstract class OperationOption {

    private OperationContext context;

    public OperationOption(OperationContext context) {
        this.context = context;
    }

    public abstract File transform(File file);
    
    protected File onGlobalTransform(File file) throws OperationException {
        throw new OperationException();
    }
}
