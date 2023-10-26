package com.abiddarris.lanfileviewer.explorer.upload;

import android.view.LayoutInflater;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.explorer.BottomNavigationProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UploadBottomNavigationProvider extends BottomNavigationProvider {
    
    public UploadBottomNavigationProvider(BottomNavigationView view) {
        super(view);
    }
    
    
    @Override
    public void onNavigationProviderShown(BottomNavigationView view) {
        super.onNavigationProviderShown(view);
        
        view.(LayoutInflater.from(view.getContext())
            .inflate(R.layout.layout_upload_button,null));
    }
    
}
