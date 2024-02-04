package com.abiddarris.lanfileviewer.actions;
import com.abiddarris.lanfileviewer.file.File;
import java.util.ArrayList;
import java.util.List;

public class SkipOption extends OperationOption {
    
    public SkipOption(OperationContext context) {
        super(context);
    }
    
    private List<String> skippedDirectoryPaths = new ArrayList<>();
    
    @Override
    public File transform(File file) {
        if(file.isDirectory()) {
            skippedDirectoryPaths.add(file.getPath());
        }
    	return null;
    }
  
    @Override
    protected File onGlobalTransform(File file) throws OperationException {
        for(String skippedDirectoryPath : skippedDirectoryPaths) {
        	if(file.getPath().startsWith(skippedDirectoryPath))
                return null;
        }
        return super.onGlobalTransform(file);
    }
    
    
}

