package com.abiddarris.lanfileviewer;

import android.os.Bundle;

import com.abiddarris.lanfileviewer.actions.runnables.DownloadManager;
import com.abiddarris.lanfileviewer.databinding.LayoutFileExplorerBinding;
import com.abiddarris.lanfileviewer.explorer.ExplorerActivity;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.ExplorerPathFragment;
import com.abiddarris.lanfileviewer.file.sharing.NetworkFileSource;
import com.abiddarris.lanfileviewer.ui.NetworkExplorerFragment;
import com.abiddarris.lanfileviewer.utils.FragmentFactoryUtils;
import com.gretta.util.log.Log;

public class FileExplorerActivity extends ExplorerActivity {

    private LayoutFileExplorerBinding binding;
    private ExplorerPathFragment pathFragment;
    
    private static final String TAG = Log.getTag(FileExplorerActivity.class);
    public static final String CONNECTION_ID = "connectionID";

    @Override
    protected void onCreate(Bundle bundle) {
        binding = LayoutFileExplorerBinding.inflate(getLayoutInflater());
        setSupportActionBar(binding.toolbar);
        setContentView(binding.getRoot());
        
        String connectionID = getIntent()
            .getExtras()
            .getString(CONNECTION_ID);
       
        NetworkFileSource source = ApplicationCore.getConnectedDevice(connectionID);
        
        pathFragment = new ExplorerPathFragment();
        
        getSupportFragmentManager()
            .setFragmentFactory(FragmentFactoryUtils.createFactory(fragmentClass -> {
                if(fragmentClass == NetworkExplorerFragment.class) {
                    ExplorerFragment fragment = create(source);
                            
                    return fragment;
                }   
                            
                return null;
            }));
     
        super.onCreate(bundle);
        
        if(bundle == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainer, create(source))
                    .commit();
        }
        
        getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.pathFragment, pathFragment)
            .commit();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        Log.debug.log(TAG, "Destroying Explorer Activity");
    }
    
    private NetworkExplorerFragment create(NetworkFileSource source) {
        NetworkExplorerFragment fragment = new NetworkExplorerFragment(source);    
        fragment.addOnExplorerCreatedListener((f,e) -> {
            pathFragment.setExplorer(e);
            e.setDownloadManager(new DownloadManager(this, source)); 
        });
        
        return fragment;
    }
    

}
