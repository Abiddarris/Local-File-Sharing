package com.abiddarris.lanfileviewer.explorer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.preference.PreferenceManager;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.abiddarris.lanfileviewer.sorter.FileSorter;
import com.gretta.util.log.Log;

public class LocalFolderSelectorActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
     
    private FolderSelectorExplorerFragment fragment;
    private ExplorerPathFragment pathFragment;
    
    @Override
    protected void onCreate(Bundle bundle) {
        setContentView(R.layout.layout_file_explorer);
        setSupportActionBar(findViewById(R.id.toolbar));
        
        getSupportFragmentManager().setFragmentFactory(new FragmentFactory(){
                @Override
                public Fragment instantiate(ClassLoader loader, String name) {
                    Class<? extends Fragment> fragmentClass = loadFragmentClass(loader,name);
                    if(fragmentClass == FolderSelectorExplorerFragment.class) {
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
        
        fragment = new FolderSelectorExplorerFragment(
            LocalFileSource.getDefaultLocalSource(this));
        fragment.setOnFolderSelectedListener((file) -> {
            Intent intent = new Intent();
            intent.putExtra(FileContract.RESULT, file.getPath());
                
            setResult(Activity.RESULT_OK, intent);
            finish();    
        });
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
    
    public static class FileContract extends ActivityResultContract<Void, File> {
        
        private FileSource source;
        
        public static final String RESULT = "result";
        
        public FileContract(FileSource source) {
            this.source = source;
        }
    
        @Override
        public Intent createIntent(Context context, Void v) {
            Intent intent = new Intent(context, LocalFolderSelectorActivity.class);
            
            return intent;
        }
        
        @Override
        public File parseResult(int resultCode, Intent intent) {
            if(intent == null) return null;
            
            String path = intent.getStringExtra(RESULT);
            if(path == null) return null;
            
            return source.getFile(path);
        }
        
    }
    
}
