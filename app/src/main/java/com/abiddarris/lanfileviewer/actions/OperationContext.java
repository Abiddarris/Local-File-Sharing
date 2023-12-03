package com.abiddarris.lanfileviewer.actions;

import com.abiddarris.lanfileviewer.file.File;
import com.gretta.util.log.Log;

public class OperationContext {
    
    public final RenameOption renameOption = new RenameOption(this);
    public final ReplaceOption replaceOption = new ReplaceOption(this);
    public final SkipOption skipOption = new SkipOption(this);
    
    public static final String TAG = Log.getTag(OperationContext.class);
    
    public File runGlobalTransform(File file) {
        Log.debug.log(TAG, "run global transform on rename option");
        try {
        	return renameOption.onGlobalTransform(file);
        } catch(OperationException err) {
        }
        
        Log.debug.log(TAG, "run global transform on replace option");
        
        try {
        	return replaceOption.onGlobalTransform(file);
        } catch(OperationException err) {
        }
        
        Log.debug.log(TAG, "run global transform on skip option");
        
        return skipOption.onGlobalTransform(file);
    }
    
}
