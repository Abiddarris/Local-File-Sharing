package com.abiddarris.lanfileviewer.explorer;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.network.NetworkFileClient;

public class NetworkExplorerFragment extends BaseExplorerFragment {
    
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item == upload) {
            getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, LocalExplorerFragment.class, null)
                .addToBackStack(null)
                .commit();
            
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
}
