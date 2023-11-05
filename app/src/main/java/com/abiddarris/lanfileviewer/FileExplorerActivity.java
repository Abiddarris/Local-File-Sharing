package com.abiddarris.lanfileviewer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import com.abiddarris.lanfileviewer.ConnectionService.ConnectionServiceBridge;
import com.abiddarris.lanfileviewer.databinding.LayoutFileExplorerBinding;
import com.abiddarris.lanfileviewer.explorer.ExplorerActivity;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.ui.NetworkExplorerFragment;
import com.abiddarris.lanfileviewer.file.network.NetworkFile;
import com.abiddarris.lanfileviewer.file.network.NetworkFileClient;
import com.gretta.util.log.Log;

public class FileExplorerActivity extends ExplorerActivity
        implements ServiceConnection {

    private LayoutFileExplorerBinding binding;
    private NetworkFileClient clientThread;
    private ConnectionService bridge;
    
    private static final String TAG = Log.getTag(FileExplorerActivity.class);
    public static final String SERVER_NAME = "serverName";

    @Override
    protected void onCreate(Bundle bundle) {
        binding = LayoutFileExplorerBinding.inflate(getLayoutInflater());
        setSupportActionBar(binding.toolbar);
        setContentView(binding.getRoot());
     
        super.onCreate(bundle);
        
        bindService(new Intent(this, ConnectionService.class), this, 0);
    }
    

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.debug.log(TAG, "Destroying Explorer Activity");

        clientThread.close();
    }

    @Override
    public void onServiceConnected(ComponentName component, IBinder binder) {
        Log.debug.log(TAG, "Connected to ConnectionService");

        String name = getIntent().getStringExtra(SERVER_NAME);

        bridge = ((ConnectionServiceBridge) binder).getService();
        Log.debug.log(TAG, "Finding server with name : " + name);
        NsdServiceInfo info = bridge.getAdapter().getServer(name);

        clientThread = new NetworkFileClient(info.getHost(), info.getPort());
        clientThread.setConnectedCallback(source -> {
            NetworkExplorerFragment fragment = new NetworkExplorerFragment(source);
                
            getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragmentContainer, fragment)
            .commit();
                
            getSupportFragmentManager().setFragmentFactory(new FragmentFactory(){
               
                @Override
                @NonNull
                public Fragment instantiate(ClassLoader loader, String name) {
                    Class<? extends Fragment> fragmentClass = loadFragmentClass(loader,name);
                    if(fragmentClass == NetworkExplorerFragment.class) {
                        return new NetworkExplorerFragment(source);
                    }   
                            
                    return super.instantiate(loader, name);
                }
                
            });
        });
        
        
        ApplicationCore app = (ApplicationCore)getApplicationContext();
        app.setNetworkFileClient(clientThread);
    }

    @Override
    public void onServiceDisconnected(ComponentName component) {}
}
