package com.abiddarris.lanfileviewer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
import com.abiddarris.lanfileviewer.file.sharing.TimeoutException;
import com.abiddarris.lanfileviewer.file.sharing.UnauthorizedException;
import com.abiddarris.lanfileviewer.settings.Settings;
import com.abiddarris.lanfileviewer.ui.ConnectingDialog;
import com.abiddarris.lanfileviewer.ui.ConnectionFailedDialog;
import com.abiddarris.lanfileviewer.ui.ExceptionDialog;
import com.abiddarris.lanfileviewer.ui.FillPasswordDialog;
import com.abiddarris.lanfileviewer.ui.NetworkExplorerFragment;
import com.abiddarris.lanfileviewer.utils.FragmentFactoryUtils;
import com.gretta.util.log.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileExplorerActivity extends ExplorerActivity
        implements ServiceConnection {

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
        viewModel.activity = this;
        observeFileSource();
        
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
    
    private void observeFileSource() {
        if(viewModel.getFileSource(this).getValue() != null) return;
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
        viewModel.executor.submit(() -> viewModel.connect(password, firstTry));
    }

    @Override
    public void onServiceDisconnected(ComponentName component) {}
    
    public static class ExplorerViewModel extends ViewModel {
        
        private FileExplorerActivity activity;
        private MutableLiveData<NetworkFileSource> source;
        private ExecutorService executor = Executors.newFixedThreadPool(1);
        
        public LiveData<NetworkFileSource> getFileSource(FileExplorerActivity activity) {
            if(source == null) {
                source = new MutableLiveData<>();
                activity.bindService(new Intent(activity, ConnectionService.class), activity, 0);
            } 
            return source;
        }
        
        private void connect(String password, boolean firstTry) {
            Bundle bundle = new Bundle();
            bundle.putString(ConnectingDialog.NAME, activity.info.getName());
        
            ConnectingDialog dialog = new ConnectingDialog();
            dialog.setArguments(bundle);
            dialog.show(activity.getSupportFragmentManager(), null);
            
            try {
                NetworkFileSource source = activity.info.openConnection(
                    activity.getApplicationContext(), password, Settings.getConnectTimeout(activity) * 1000L);
                
                Log.debug.log(TAG, "server id " + source.getServerId());
                this.source.postValue(source);      
            } catch (UnauthorizedException e) {
                if(!firstTry) {
                    activity.runOnUiThread(() -> Toast.makeText(
                    activity, R.string.wrong_password, Toast.LENGTH_SHORT
                    ).show());
                }
                new FillPasswordDialog().show(activity.getSupportFragmentManager(), null);
            } catch(AccessRejectedException e) {
                showConnectionFailedDialog(activity.getString(R.string.access_denied));
            } catch(TimeoutException e) {
                showConnectionFailedDialog(activity.getString(R.string.timeout));
            } catch(Exception e) {
                new ExceptionDialog(e)
                    .show(activity.getSupportFragmentManager(), null);
                Log.debug.log(TAG, e);
            } finally {
                dialog.dismiss();
            }
        }
        
        private void showConnectionFailedDialog(String message) {
            Bundle bundle = new Bundle();
            bundle.putString(ConnectionFailedDialog.MESSAGE, message);
        
            ConnectionFailedDialog dialog = new ConnectionFailedDialog();
            dialog.setArguments(bundle);
            dialog.show(activity.getSupportFragmentManager(), null);
        }
        
        @Override
        protected void onCleared() {
            super.onCleared();
            
            activity = null;
            executor.shutdownNow();
        }
        
    
    }
    
}
