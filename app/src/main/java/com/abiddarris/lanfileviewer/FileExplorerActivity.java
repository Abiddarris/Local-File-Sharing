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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.abiddarris.lanfileviewer.ConnectionService.ConnectionServiceBridge;
import com.abiddarris.lanfileviewer.actions.runnables.DownloadManager;
import com.abiddarris.lanfileviewer.databinding.LayoutFileExplorerBinding;
import com.abiddarris.lanfileviewer.explorer.ExplorerActivity;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.ExplorerPathFragment;
import com.abiddarris.lanfileviewer.file.sharing.AccessRejectedException;
import com.abiddarris.lanfileviewer.file.sharing.NetworkFileSource;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice;
import com.abiddarris.lanfileviewer.file.sharing.UnauthorizedException;
import com.abiddarris.lanfileviewer.ui.AccessRejectedDialog;
import com.abiddarris.lanfileviewer.ui.ConnectingDialog;
import com.abiddarris.lanfileviewer.ui.ExceptionDialog;
import com.abiddarris.lanfileviewer.ui.FillPasswordDialog;
import com.abiddarris.lanfileviewer.ui.NetworkExplorerFragment;
import com.abiddarris.lanfileviewer.utils.FragmentFactoryUtils;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileExplorerActivity extends ExplorerActivity
        implements ServiceConnection {

    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private ExplorerViewModel viewModel;
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
        
        viewModel = new ViewModelProvider(this)
            .get(ExplorerViewModel.class);
        if(bundle == null) {
            onFirstCreate();
        }
        
        getSupportFragmentManager()
            .setFragmentFactory(FragmentFactoryUtils.createFactory(fragmentClass -> {
                if(fragmentClass == NetworkExplorerFragment.class) {
                    ExplorerFragment fragment = create(viewModel.source.getValue());
                            
                    return fragment;
                }   
                            
                return null;
            }));
     
        super.onCreate(bundle);
        
        getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.pathFragment, pathFragment)
            .commit();
    }
    
    private void onFirstCreate() {
        viewModel.getFileSource(this)
            .observe(this, source -> {
                getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainer, create(source))
                    .commit();
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        executor.shutdownNow();
        
        Log.debug.log(TAG, "Destroying Explorer Activity");
    }

    @Override
    public void onServiceConnected(ComponentName component, IBinder binder) {
        Log.debug.log(TAG, "Connected to ConnectionService");

        String name = getIntent().getStringExtra(SERVER_NAME);

        bridge = ((ConnectionServiceBridge) binder).getService();
        Log.debug.log(TAG, "Finding server with name : " + name);
        
        info = bridge.getAdapter().getServer(name);
        
        connectAsync(null, true);
        unbindService(this);
    }
    
    private NetworkExplorerFragment create(NetworkFileSource source) {
        NetworkExplorerFragment fragment = new NetworkExplorerFragment(source);    
        fragment.addOnExplorerCreatedListener((f,e) -> {
            pathFragment.setExplorer(e);
            e.setDownloadManager(new DownloadManager(this, source)); 
        });
        
        return fragment;
    }
    
    public void connectAsync(String password) {
        connectAsync(password, false);
    }
    
    public void connectAsync(String password, boolean firstTry) {
        executor.submit(() -> connect(password, firstTry));
    }
    
    private void connect(String password, boolean firstTry) {
        Bundle bundle = new Bundle();
        bundle.putString(ConnectingDialog.NAME, info.getName());
        
        ConnectingDialog dialog = new ConnectingDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), null);
        try {
            NetworkFileSource source = password == null ? info.openConnection(this) : info.openConnection(this, password);
                
            Log.debug.log(TAG, "server id " + source.getServerId());
            viewModel.source.postValue(source);      
        } catch (UnauthorizedException e) {
            if(!firstTry) {
                runOnUiThread(() -> Toast.makeText(
                    this, R.string.wrong_password, Toast.LENGTH_SHORT
                ).show());
            }
            new FillPasswordDialog()
                .show(getSupportFragmentManager(), null);
        } catch(AccessRejectedException e) {
            new AccessRejectedDialog()
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
    
    public static class ExplorerViewModel extends ViewModel {
        
        private MutableLiveData<NetworkFileSource> source;
        
        public LiveData<NetworkFileSource> getFileSource(FileExplorerActivity activity) {
            if(source == null) {
                source = new MutableLiveData<>();
                activity.bindService(new Intent(activity, ConnectionService.class), activity, 0);
            } 
            return source;
        }
    }
    
}
