package com.abiddarris.lanfileviewer.file.local;

import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.RootFile;
import com.abiddarris.lanfileviewer.file.local.LocalFile;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;

public class LocalRootFile extends LocalFile implements RootFile {

    LocalRootFile(LocalFileSource source, File parent, java.io.File file) {
        super(source, parent, file);
    }
    
}
