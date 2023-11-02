package com.abiddarris.lanfileviewer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.explorer.ExplorerSelectDialog;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.LocalSelectorExplorerActivity;
import com.abiddarris.lanfileviewer.explorer.LocalSelectorExplorerFragment;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.network.NetworkFileClient;

public class NetworkExplorerFragment extends ExplorerFragment {
    
    private MenuItem upload;
    
    public NetworkExplorerFragment() {
        super();
    }
    
    @Override
    public FileSource getSource() {
        ApplicationCore app = (ApplicationCore)getContext().getApplicationContext();
        NetworkFileClient client = app.getNetworkFileClient();
        return client.getSource();
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
            bundle.putString(LocalSelectorExplorerFragment.ACTION_TEXT, getString(R.string.upload));
            
            Intent intent = new Intent(getContext(), LocalSelectorExplorerActivity.class);
            intent.putExtra("extra", bundle);
            startActivity(intent);
            
            /*new ExplorerSelectDialog()
                .show(getChildFragmentManager(), null);
           */
            
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
