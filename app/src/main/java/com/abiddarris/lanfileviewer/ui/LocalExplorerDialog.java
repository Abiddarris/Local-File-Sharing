package com.abiddarris.lanfileviewer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.preference.PreferenceManager;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.explorer.ExplorerActivity;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.ExplorerPathFragment;
import com.abiddarris.lanfileviewer.explorer.SelectorExplorerFragment;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.abiddarris.lanfileviewer.sorter.FileSorter;

public class LocalExplorerDialog extends ExplorerActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private ExplorerFragment fragment;
    private ExplorerPathFragment pathFragment;
    
    @Override
    protected void onCreate(Bundle bundle) {
        setContentView(R.layout.layout_file_explorer);
        setSupportActionBar(findViewById(R.id.toolbar));
        
        getSupportFragmentManager().setFragmentFactory(new FragmentFactory(){
                @Override
                public Fragment instantiate(ClassLoader loader, String name) {
                    Class<? extends Fragment> fragmentClass = loadFragmentClass(loader,name);
                    if(fragmentClass == SelectorExplorerFragment.class) {
                        return initFragment(bundle);
                    }
                    
                    return super.instantiate(loader, name);
                }
        });
        
        super.onCreate(bundle);
        
        pathFragment = new ExplorerPathFragment();
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.pathFragment, pathFragment)
                .commit();
        
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragmentContainer, initFragment(bundle))
                .commit();
    }
    
    private ExplorerFragment initFragment(Bundle bundle) {
        int sortType = PreferenceManager.getDefaultSharedPreferences(this)
            .getInt(SortByDialog.SORT_TYPE, FileSorter.NAME | FileSorter.ASCENDING);
        
        fragment = new SelectorExplorerFragment(
            LocalFileSource.getDefaultLocalSource(this));
        fragment.setArguments(getIntent().getBundleExtra("extra"));
        fragment.setSorter(FileSorter.createSorter(sortType));
        fragment.addOnExplorerCreatedListener((f,e) -> {
            
            pathFragment.setExplorer(e);    
        });
        
        return fragment;
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if(key.equals(SortByDialog.SORT_TYPE)) {
            int sortType = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(SortByDialog.SORT_TYPE, FileSorter.NAME | FileSorter.ASCENDING);
            fragment.setSorter(FileSorter.createSorter(sortType));
            fragment.getExplorer()
                .update();
            
            
        }
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onResume() {
        super.onResume();
        
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this);
    }
    
    
}
