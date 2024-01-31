package com.abiddarris.lanfileviewer.actions;

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
    
    private Map<File, File> renamedFolders = new HashMap<>();
    
    public RenameOption(OperationContext context) {
        super(context);
    }
    
    @Override
    public File transform(File file) {
        int index = 0;
        File renamedFile;
        
    	do {
            index++;
            String nameWithoutExtension = Files.getNameWithoutExtension(file) + " (" + index + ")";
            String extension = Files.getExtension(file);
            String name = extension.isEmpty() ? nameWithoutExtension : nameWithoutExtension + "." + extension;
            
            renamedFile = file.getSource()
                    .getFile(file.getParentFile().getPath() + "/" + name);
            try {
                renamedFile.updateDataSync(REQUEST_EXISTS);
            } catch(Exception err) {
                Log.err.log(TAG, err);
            }
        } while (renamedFile.exists());
        
        if(file.isDirectory()) {
            renamedFolders.put(file,renamedFile);
        }
        
        return renamedFile;
    }
    
    @Override
    protected File onGlobalTransform(File file) throws OperationException {
        for(File originalFolder : renamedFolders.keySet()) {
        	String originalFolderPath = originalFolder.getPath();
            if(file.getPath().startsWith(originalFolderPath)) {
                File renamedFolder = renamedFolders.get(originalFolder);
                String newPath = file.getPath().replace(originalFolderPath, renamedFolder.getPath());
                
                return file.getSource().getFile(newPath);
            }
        }
        throw new OperationException();
    }
    
}
