package com.abiddarris.lanfileviewer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.actions.runnables.UploadRunnable;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.FilesSelectorFragment;
import com.abiddarris.lanfileviewer.explorer.LocalFilesSelectorActivity;
import com.abiddarris.lanfileviewer.explorer.SortByDialog;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;

import com.abiddarris.lanfileviewer.sorter.FileSorter;

public class NetworkExplorerFragment extends ExplorerFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private ActivityResultLauncher<Bundle> uploadLauncher;
    private MenuItem upload;
    
    public NetworkExplorerFragment(FileSource source) {
        super(source);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onCreate(Bundle bundle) {
        uploadLauncher = registerForActivityResult(
            new LocalFilesSelectorActivity.FileContract(getContext()), new FilesCallback());
        int sortType = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(SortByDialog.SORT_TYPE, FileSorter.NAME | FileSorter.ASCENDING);
            setSorter(FileSorter.createSorter(sortType));
    
        super.onCreate(bundle);
    }
   
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        
        upload = menu.add(getString(R.string.upload));
    }
    
    @Override
    @MainThread
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == upload) {
            Bundle bundle = new Bundle();
            bundle.putString(ExplorerFragment.TITLE, getString(R.string.upload));
            bundle.putString(FilesSelectorFragment.ACTION_TEXT, getString(R.string.upload));
            
            uploadLauncher.launch(bundle);
           
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if(key.equals(SortByDialog.SORT_TYPE)) {
            int sortType = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(SortByDialog.SORT_TYPE, FileSorter.NAME | FileSorter.ASCENDING);
            setSorter(FileSorter.createSorter(sortType));
            getExplorer().refresh();
        }
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onResume() {
        super.onResume();
        
        PreferenceManager.getDefaultSharedPreferences(getContext())
            .registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        
        PreferenceManager.getDefaultSharedPreferences(getContext())
            .unregisterOnSharedPreferenceChangeListener(this);
    }
    
    private class FilesCallback implements ActivityResultCallback<FilePointer[]>{
        
        @Override
        public void onActivityResult(FilePointer[] pointers) {
            if(pointers == null) return;
        
            ActionRunnable runnable = new UploadRunnable(getSource(), getExplorer().getParent(), pointers);
            new ActionDialog(getExplorer(), runnable)
                .show(getChildFragmentManager(), null);
        }
        
    }
        
    
}
