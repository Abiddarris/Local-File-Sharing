package com.abiddarris.lanfileviewer.explorer;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;

public class LocalExplorerFragment extends ExplorerFragment {
    
    public LocalExplorerFragment() {
        super();
    }
    
    @Override
    public FileSource getSource() {
        return LocalFileSource.getDefaultLocalSource(getContext());
    }
    
}
