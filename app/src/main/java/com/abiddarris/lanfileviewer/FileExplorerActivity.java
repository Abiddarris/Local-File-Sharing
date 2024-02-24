package com.abiddarris.lanfileviewer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import com.abiddarris.lanfileviewer.ConnectionService.ConnectionServiceBridge;
import com.abiddarris.lanfileviewer.actions.runnables.DownloadManager;
import com.abiddarris.lanfileviewer.databinding.LayoutFileExplorerBinding;
import com.abiddarris.lanfileviewer.explorer.ExplorerActivity;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.ExplorerPathFragment;
import com.abiddarris.lanfileviewer.file.sharing.NetworkFileSource;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice;
import com.abiddarris.lanfileviewer.file.sharing.UnauthorizedException;
import com.abiddarris.lanfileviewer.ui.ConnectingDialog;
import com.abiddarris.lanfileviewer.ui.ExceptionDialog;
import com.abiddarris.lanfileviewer.ui.FillPasswordDialog;
import com.abiddarris.lanfileviewer.ui.NetworkExplorerFragment;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileExplorerActivity extends ExplorerActivity
        implements ServiceConnection {

    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private LayoutFileExplorerBinding binding;
    private ConnectionService bridge;
    private ExplorerPathFragment pathFragment;
    private SharingDevice info;
    
    private static final String TAG = Log.getTag(FileExplorerActivity.class);
    public static final String SERVER_NAME = "serverName";

    @Override
    protected void onCreate(Bundle bundle) {
        binding = LayoutFileExplorerBinding.inflate(getLayoutInflater());
        setSupportActionBar(binding.toolbar);
        setContentView(binding.getRoot());
        
        pathFragment = new ExplorerPathFragment();
        
        getSupportFragmentManager().setFragmentFactory(new FragmentFactory(){
                    @Override
                    @NonNull
                    public Fragment instantiate(ClassLoader loader, String name) {
                        Class<? extends Fragment> fragmentClass = loadFragmentClass(loader,name);
                        if(fragmentClass == NetworkExplorerFragment.class) {
                            ApplicationCore core = (ApplicationCore)getApplication();
                            ExplorerFragment fragment = create(core.getCurrentFileSource());
                            
                            return fragment;
                        }   
                            
                        return super.instantiate(loader, name);
                    }
                });
     
        super.onCreate(bundle);
        
        getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.pathFragment, pathFragment)
            .commit();
        
        bindService(new Intent(this, ConnectionService.class), this, 0);
    }
    

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.debug.log(TAG, "Destroying Explorer Activity");
    }

    @Override
    public void onServiceConnected(ComponentName component, IBinder binder) {
        Log.debug.log(TAG, "Connected to ConnectionService");

        String name = getIntent().getStringExtra(SERVER_NAME);

        bridge = ((ConnectionServiceBridge) binder).getService();
        Log.debug.log(TAG, "Finding server with name : " + name);
        
        info = bridge.getAdapter().getServer(name);
        
        for(Fragment fragment : getSupportFragmentManager().getFragments()) {
        	if(fragment.getClass() == NetworkExplorerFragment.class)
                return;
        }
        
        connectAsync(null, true);
    }
    
    private NetworkExplorerFragment create(NetworkFileSource source) {
        NetworkExplorerFragment fragment = new NetworkExplorerFragment(source);    
        fragment.addOnExplorerCreatedListener((f,e) -> {
            pathFragment.setExplorer(e);
            e.setDownloadManager(new DownloadManager(this, source)); 
        });
         
        ((ApplicationCore) getApplication())
            .setCurrentFileSource(source);
        
        return fragment;
    }
    
    public void connectAsync(String password) {
        connectAsync(password, false);
    }
    
    public void connectAsync(String password, boolean firstTry) {
        executor.submit(() -> connect(password, firstTry));
    }
    
    private void connect(String password, boolean firstTry) {
        ConnectingDialog dialog = new ConnectingDialog();
        dialog.show(getSupportFragmentManager(), null);
        try {
            NetworkFileSource source = password == null ? info.openConnection(this) : info.openConnection(this, password);
                
            Log.debug.log(TAG, "server id " + source.getServerId());
                    
            getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragmentContainer, create(source))
                .commit();
        } catch (UnauthorizedException e) {
            if(!firstTry) {
                runOnUiThread(() -> Toast.makeText(
                    this, R.string.wrong_password, Toast.LENGTH_SHORT
                ).show());
            }
            new FillPasswordDialog()
                .show(getSupportFragmentManager(), null);
        } catch(Exception e) {
            new ExceptionDialog(e)
                .show(getSupportFragmentManager(), null);
            Log.debug.log(TAG, e);
        } finally {
            dialog.dismiss();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName component) {}
}
