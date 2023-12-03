package com.abiddarris.lanfileviewer.actions;

import com.abiddarris.lanfileviewer.file.File;

public class ReplaceOption extends OperationOption{
    
    protected ReplaceOption(OperationContext context) {
        super(context);
    }
    
    @Override
    public File transform(File file) {
        return file;
    }
    
    @Override
    protected File onGlobalTransform(File file) throws OperationException {
        throw new OperationException();
    }
    
}
