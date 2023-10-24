package com.abiddarris.lanfileviewer.explorer;

import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;

public class LocalExplorerFragment extends BaseExplorerFragment {
    
    public LocalExplorerFragment() {
        super();
    }
    
    @Override
    public FileSource getSource() {
        return LocalFileSource.getDefaultLocalSource(getContext());
    }
    
    @Override
    public void showTopNavigationView(TopNavigationView.Callback callback) {
        // TODO: Implement this method
    }
    
}
