package com.abiddarris.lanfileviewer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.abiddarris.lanfileviewer.settings.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);
        
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, SettingsFragment.class, null)
            .commit();
    }
    
    
}
