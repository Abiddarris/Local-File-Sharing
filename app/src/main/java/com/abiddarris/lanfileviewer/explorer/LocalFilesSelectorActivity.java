package com.abiddarris.lanfileviewer.explorer;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import androidx.activity.result.contract.ActivityResultContract;
import com.abiddarris.lanfileviewer.file.File;
import android.app.Activity;
import android.content.Context;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.sorter.FileSorter;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import androidx.appcompat.app.AppCompatActivity;
import com.gretta.util.log.Log;

public class LocalFilesSelectorActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    public static final String TAG = Log.getTag(LocalFilesSelectorActivity.class);
    
    private SelectorFragment fragment;
    private ExplorerPathFragment pathFragment;
    
    @Override
    protected void onCreate(Bundle bundle) {
        setContentView(R.layout.layout_file_explorer);
        setSupportActionBar(findViewById(R.id.toolbar));
        
        getSupportFragmentManager().setFragmentFactory(new FragmentFactory(){
                @Override
                public Fragment instantiate(ClassLoader loader, String name) {
                    Class<? extends Fragment> fragmentClass = loadFragmentClass(loader,name);
                    if(fragmentClass == FilesSelectorFragment.class) {
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
        
        fragment = new FilesSelectorFragment(
            LocalFileSource.getDefaultLocalSource(this));
        fragment.setOnSelectedListener((files) -> {
            String[] paths = new String[files.length];
            for(int i = 0; i < paths.length; ++i) {
                paths[i] = files[i].getPath();
            }    
                
            Intent intent = new Intent();
            intent.putExtra(FileContract.RESULT, paths);
            
            setResult(RESULT_OK, intent);
            finish();    
        });
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
                .refresh();
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
    
    public static class FileContract extends ActivityResultContract<Bundle, FilePointer[]> {
        
        public static final String RESULT = "result";
    
        private Context context;
        
        public FileContract(Context context) {
            this.context = context;
        }
        
        @Override
        public Intent createIntent(Context context, Bundle bundle) {
            Intent intent = new Intent(context, LocalFilesSelectorActivity.class);
            intent.putExtra("extra", bundle);
            return intent;
        }
        
        @Override
        public FilePointer[] parseResult(int resultCode, Intent intent) {
            Log.debug.log(TAG, "result code : " + resultCode);
            
            if(intent == null) return null;
            
            String[] paths = intent.getStringArrayExtra(RESULT);
            if(paths == null) return null;
            
            FileSource source = FileSource.getDefaultLocalSource(context);
            
            FilePointer[] files = new FilePointer[paths.length];
            for(int i = 0; i < files.length; ++i) {
            	files[i] = source.getFilePointer(paths[i]);
            }
            
            return files;
        }
        
    }
}
