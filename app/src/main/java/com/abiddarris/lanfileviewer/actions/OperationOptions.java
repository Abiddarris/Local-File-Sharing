package com.abiddarris.lanfileviewer.actions;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;
import com.gretta.util.log.Log;

public enum OperationOptions {
    
    RENAME() {
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
    },
    
    SKIP() {
        @Override
        public File transform(File file) {
        	return null;
        }
    },
    
    REPLACE() {
        @Override
        public File transform(File file) {
        	return file;
        }
    };
    
    public static final String TAG = Log.getTag(OperationOptions.class);
    
    public File transform(OperationContext context, File file) {
        return null;
    }
    
    protected abstract File transform(File file);

}
