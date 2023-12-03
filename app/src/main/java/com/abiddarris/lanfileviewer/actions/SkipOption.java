package com.abiddarris.lanfileviewer.actions;
import com.abiddarris.lanfileviewer.file.File;
import java.util.ArrayList;
import java.util.List;

public class SkipOption extends OperationOption {
    
    public SkipOption(OperationContext context) {
        super(context);
    }
    
    private List<File> skippedDirectories = new ArrayList<>();
    
    @Override
    public File transform(File file) {
        if(file.isDirectory()) {
            skippedDirectories.add(file);
        }
    	return null;
    }
  
    @Override
    protected File onGlobalTransform(File file) throws OperationException {
        for(File skippedDirectory : skippedDirectories) {
        	if(file.getPath().startsWith(skippedDirectory.getPath()))
                return null;
        }
        return super.onGlobalTransform(file);
    }
    
    
}
