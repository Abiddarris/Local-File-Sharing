package com.abiddarris.lanfileviewer.explorer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ExplorerActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().setFragmentResult(ExplorerFragment.BACK_PRESSED_EVENT, new Bundle());
        return true;
    }
    
}
