package com.abiddarris.lanfileviewer.actions;

import com.abiddarris.lanfileviewer.file.FileSource;
import static com.abiddarris.lanfileviewer.file.Requests.*;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenameOption extends OperationOption {
    
    public static final String TAG = Log.getTag(RenameOption.class);
    
    private Map<String, String> renamedFolderPaths = new HashMap<>();
    
    public RenameOption(OperationContext context) {
        super(context);
    }
    
    @Override
    public File transform(File file) {
        int index = 0;
        File renamedFile = null;
        
    	do {
            if(renamedFile != null) {
                FileSource.freeFiles(renamedFile);
            }
            index++;
            String nameWithoutExtension = Files.getNameWithoutExtension(file) + " (" + index + ")";
            String extension = Files.getExtension(file);
            String name = extension.isEmpty() ? nameWithoutExtension : nameWithoutExtension + "." + extension;
            
            renamedFile = file.getSource()
                    .getFile(file.getParent() + "/" + name);
            try {
                renamedFile.updateDataSync(REQUEST_EXISTS);
            } catch(Exception err) {
                Log.err.log(TAG, err);
            }
        } while (renamedFile.exists());
        
        if(file.isDirectory()) {
            renamedFolderPaths.put(file.getPath(),renamedFile.getPath());
        }
        
        return renamedFile;
    }
    
    @Override
    protected File onGlobalTransform(File file) throws OperationException {
        for(String originalFolderPath : renamedFolderPaths.keySet()) {
            if(file.getPath().startsWith(originalFolderPath)) {
                String renamedFolderPath = renamedFolderPaths.get(originalFolderPath);
                String newPath = file.getPath()
                    .replace(originalFolderPath, renamedFolderPath);
                
                return file.getSource().getFile(newPath);
            }
        }
        throw new OperationException();
    }
    
}
