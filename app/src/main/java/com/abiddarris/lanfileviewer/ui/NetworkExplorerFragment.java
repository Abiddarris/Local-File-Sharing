package com.abiddarris.lanfileviewer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.SelectorExplorerFragment;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.abiddarris.lanfileviewer.file.sharing.NetworkFileClient;
import com.abiddarris.lanfileviewer.ui.actions.UploadDialog;

public class NetworkExplorerFragment extends ExplorerFragment {
    
    private ActivityResultLauncher<Bundle> uploadLauncher = registerForActivityResult(
        new SelectorExplorerFragment.FileContract(LocalFileSource.getDefaultLocalSource(getContext()), LocalExplorerDialog.class), new ActivityResultCallback<File[]>(){
            @Override
            public void onActivityResult(File[] files) {
                new UploadDialog(getSource(),getExplorer().getParent(),files)
                    .show(getChildFragmentManager(), null);
            }
        });
    
    private MenuItem upload;
    
    public NetworkExplorerFragment(FileSource source) {
        super(source);
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
