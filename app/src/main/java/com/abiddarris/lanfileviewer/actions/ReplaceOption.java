package com.abiddarris.lanfileviewer.actions;

import com.abiddarris.lanfileviewer.file.File;

public class ReplaceOption implements OperationOption{
    
    @Override
    public File transform(File file) {
    	return file;
    }
    
}
