package com.abiddarris.lanfileviewer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.actions.uploads.UploadRunnable;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.SelectorExplorerFragment;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.abiddarris.lanfileviewer.file.sharing.NetworkFileClient;

public class NetworkExplorerFragment extends ExplorerFragment {
    
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
            new SelectorExplorerFragment.FileContract(LocalFileSource.getDefaultLocalSource(getContext()), LocalExplorerDialog.class), new ActivityResultCallback<File[]>(){
                @Override
                public void onActivityResult(File[] files) {
                    if(files == null) return;
                    
                    ActionRunnable runnable = new UploadRunnable(getSource(), getExplorer().getParent(), files);
                    new ActionDialog(runnable)
                        .show(getChildFragmentManager(), null);
                }
        });
    
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
            bundle.putString(SelectorExplorerFragment.ACTION_TEXT, getString(R.string.upload));
            
            uploadLauncher.launch(bundle);
           
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
