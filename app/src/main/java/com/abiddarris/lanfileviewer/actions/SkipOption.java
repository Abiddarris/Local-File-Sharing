package com.abiddarris.lanfileviewer.actions;
import com.abiddarris.lanfileviewer.file.File;

public class SkipOption implements OperationOption {
    
    @Override
    public File transform(File file) {
    	return null;
    }
    
}
