package com.abiddarris.lanfileviewer.file;

import com.gretta.util.recycler.ObjectRecycler;
import com.gretta.util.recycler.ReferencePolicy;

class FilePointerSource extends ObjectRecycler<String, FilePointer> {

    private FileSource source;
    
    FilePointerSource(FileSource source) {
        this.source = source;
        
        addPolicies(ReferencePolicy.MULTIPLE_REFERENCE);
    }
    
    @Override
    protected FilePointer create(String path) {
        return new FilePointer(source, path);
    }
    

    @Override
    protected FilePointer get(String path) {
        return super.get(path);
    }
}
