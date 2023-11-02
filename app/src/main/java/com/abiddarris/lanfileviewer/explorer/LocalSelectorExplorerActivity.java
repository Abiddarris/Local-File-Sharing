package com.abiddarris.lanfileviewer.explorer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.abiddarris.lanfileviewer.R;

public class LocalSelectorExplorerActivity extends ExplorerActivity {
    
    @Override
    protected void onCreate(Bundle bundle) {
        setContentView(R.layout.layout_file_explorer);
        setSupportActionBar(findViewById(R.id.toolbar));
        
        super.onCreate(bundle);
        
        getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragmentContainer, LocalSelectorExplorerFragment.class, getIntent().getBundleExtra("extra"))
            .commit();
    }
    
    
}
