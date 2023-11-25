package com.abiddarris.lanfileviewer.actions;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;
import com.gretta.util.log.Log;

public interface OperationOption{
    
    File transform(File file);

}
