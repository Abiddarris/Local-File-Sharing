package com.abiddarris.lanfileviewer.explorer;

import android.view.View;

public abstract class TopNavigationView {
    
    public abstract void finish();
    
    public abstract void setCustomView(View view);
    
    public static interface Callback {
        
        void onShow(TopNavigationView view);
        
        void onHide(TopNavigationView view);
        
    }
    
}
