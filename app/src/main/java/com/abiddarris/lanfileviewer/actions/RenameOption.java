package com.abiddarris.lanfileviewer.actions;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;
import com.gretta.util.log.Log;

public class RenameOption extends OperationOption {
    
    public static final String TAG = Log.getTag(RenameOption.class);
    
    public RenameOption(OperationContext context) {
        super(context);
    }
    
    @Override
    public File transform(File file) {
        int index = 1;
        File renamedFile;
        
    	do {
            String name = Files.getNameWithoutExtension(file) + " (" + index + ")" +
                Files.getExtension(file);
               
            renamedFile = file.getSource()
                    .getFile(file.getParentFile().getPath() + "/" + name);
            try {
                renamedFile.updateDataSync();
            } catch(Exception err) {
                Log.err.log(TAG, err);
            }
        } while (renamedFile.exists());
        return renamedFile;
    }
    
    @Override
    protected File onGlobalTransform(File file) throws OperationException {
        throw new OperationException();
    }
    
}
