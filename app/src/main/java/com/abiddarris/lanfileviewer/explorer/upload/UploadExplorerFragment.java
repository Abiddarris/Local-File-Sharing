package com.abiddarris.lanfileviewer.explorer.upload;

import android.os.Bundle;
import android.view.View;
import com.abiddarris.lanfileviewer.explorer.BottomNavigationProvider;
import com.abiddarris.lanfileviewer.explorer.LocalExplorerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UploadExplorerFragment extends LocalExplorerFragment {
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceBundle) {
        super.onViewCreated(view, savedInstanceBundle);
        
        requireActivity().setTitle("Upload");
    }
    
   
    
}
