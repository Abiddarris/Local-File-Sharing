package com.abiddarris.lanfileviewer.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import com.abiddarris.lanfileviewer.explorer.ExplorerActivity;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.explorer.SelectorExplorerFragment;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;

public class LocalExplorerDialog extends ExplorerActivity {
     
    @Override
    protected void onCreate(Bundle bundle) {
        setContentView(R.layout.layout_file_explorer);
        setSupportActionBar(findViewById(R.id.toolbar));
        
        super.onCreate(bundle);
        
        Fragment fragment = new SelectorExplorerFragment(
            LocalFileSource.getDefaultLocalSource(this));
        fragment.setArguments(getIntent().getBundleExtra("extra"));
        
        getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragmentContainer, fragment)
            .commit();
        
        getSupportFragmentManager().setFragmentFactory(new FragmentFactory(){
                
                @Override
                public Fragment instantiate(ClassLoader loader, String name) {
                    Class<? extends Fragment> fragmentClass = loadFragmentClass(loader,name);
                    if(fragmentClass == SelectorExplorerFragment.class) {
                        Fragment fragment = new SelectorExplorerFragment(
                            LocalFileSource.getDefaultLocalSource(LocalExplorerDialog.this));
                        fragment.setArguments(getIntent().getBundleExtra("extra"));
                    }
                    
                    return super.instantiate(loader, name);
                }
                
        });
    }
    
    
}
