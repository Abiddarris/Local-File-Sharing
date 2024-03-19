package com.abiddarris.lanfileviewer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.ConnectionService;
import com.abiddarris.lanfileviewer.FileExplorerActivity;
import com.abiddarris.lanfileviewer.MainActivity;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.ScanResult;
import com.abiddarris.lanfileviewer.databinding.FragmentScanBinding;
import com.abiddarris.lanfileviewer.file.sharing.AccessRejectedException;
import com.abiddarris.lanfileviewer.file.sharing.NetworkFileSource;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice;
import com.abiddarris.lanfileviewer.file.sharing.TimeoutException;
import com.abiddarris.lanfileviewer.file.sharing.UnauthorizedException;
import com.abiddarris.lanfileviewer.settings.Settings;
import com.gretta.util.log.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {
    
    private ConnectionService service;
    private ConnectViewModel viewModel;
    private FragmentScanBinding binding;
    
    private static final String TAG = Log.getTag(ScanFragment.class);
    
    public ScanFragment() {
        super(R.layout.fragment_scan);
    }
    
    @Override
    @MainThread
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        
        viewModel = new ViewModelProvider(getActivity())
            .get(ConnectViewModel.class);
        viewModel.init(this);
        viewModel.getFileSource()
            .observe(this, source -> {
                if(source == null) return;
                
                ApplicationCore.addConnectedDevice(viewModel.device, source);
                openExplorer(ApplicationCore.getConnectionID(viewModel.device));
                
                viewModel.reset();
            });
        
        binding = FragmentScanBinding.bind(view);
        binding.scanButton.setOnClickListener((v) -> {
            if (!service.isScanning()) {
                binding.scanButton.setText(getString(R.string.stop_scan));
                service.scanServer();
                return;
            }
            binding.scanButton.setText(getString(R.string.start_scan));
            service.stopScanServer();
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.sharingDevices.setLayoutManager(layoutManager);
        
        MainActivity activity = (MainActivity)getActivity();
        activity.addConnectedListener((bridge) -> {
            service = bridge;    
                
            ScanResult result = service.getScan();
            ServerListAdapter adapter = new ServerListAdapter(getContext(), result.getResults());
                
            result.addUpdatedListener(adapter);
                
            adapter.setOnServerSelectedListener(device -> connectToDevice(device));
             
            binding.sharingDevices.setAdapter(adapter);
        });
    }
    
    private void connectToDevice(SharingDevice device) {
        String ID = ApplicationCore.getConnectionID(device);
        if(ID != null) {
            openExplorer(ID);
            return;
        }
        
        viewModel.device = device;
        viewModel.connectAsync(null, true);
    }
    
    private void openExplorer(String ID) {
        binding.scanButton.setText(R.string.start_scan);
        
        Intent intent = new Intent(getContext(), FileExplorerActivity.class);
        intent.putExtra(FileExplorerActivity.CONNECTION_ID, ID);
        startActivity(intent);
    }
    
    public static class ConnectViewModel extends ViewModel {
        
        private MainActivity activity;
        private MutableLiveData<NetworkFileSource> source;
        private ExecutorService executor = Executors.newFixedThreadPool(1);
        private ScanFragment fragment;
        private SharingDevice device;
        
        public void connectAsync(String password) {
            connectAsync(password, false);
        }
        
        private void init(ScanFragment fragment) {
        	this.fragment = fragment;
            
            activity = (MainActivity) fragment.getActivity();
        }
        
        private LiveData<NetworkFileSource> getFileSource() {
            if(source == null) {
                source = new MutableLiveData<>();
            } 
            return source;
        }
        
        private void connectAsync(String password, boolean firstTry) {
            executor.submit(() -> connect(password, firstTry));
        }
        
        private void connect(String password, boolean firstTry) {
            Bundle bundle = new Bundle();
            bundle.putString(ConnectingDialog.NAME, device.getName());
        
            final String CONNECT_DIALOG = "connectDialog";
            
            ConnectingDialog dialog = new ConnectingDialog();
            dialog.setArguments(bundle);
            dialog.show(activity.getSupportFragmentManager(), CONNECT_DIALOG);
            
            try {
                NetworkFileSource source = device.openConnection(
                    activity.getApplicationContext(), password, Settings.getConnectTimeout(activity) * 1000);
                
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
                Fragment fragment = activity.getSupportFragmentManager()
                    .findFragmentByTag(CONNECT_DIALOG);
                if(!(fragment instanceof DialogFragment)) return;
                dialog = (ConnectingDialog) fragment;
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
        
        private void reset() {
            source.setValue(null);
            device = null;
        }
        
        @Override
        protected void onCleared() {
            super.onCleared();
            
            activity = null;
            fragment = null;
            executor.shutdownNow();
        }
    
    }
    
}
