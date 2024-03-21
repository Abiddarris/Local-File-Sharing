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
import com.abiddarris.lanfileviewer.file.sharing.ConnectProperties;
import com.abiddarris.lanfileviewer.file.sharing.NetworkFileSource;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice.CancellationSignal;
import com.abiddarris.lanfileviewer.file.sharing.TimeoutException;
import com.abiddarris.lanfileviewer.file.sharing.UnauthorizedException;
import com.abiddarris.lanfileviewer.settings.Settings;
import com.gretta.util.log.Log;

import java.io.InterruptedIOException;

public class ScanFragment extends Fragment {
    
    private ConnectionService service;
    private ConnectViewModel viewModel;
    private FragmentScanBinding binding;
    private ServerListAdapter adapter;
    
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
                
                int index = adapter.getIndex(viewModel.device);
                if(index >= 0)
                    adapter.notifyItemRemoved(index);
                
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
            adapter = new ServerListAdapter(getContext(), result.getResults());
                
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
        viewModel.connect(null, true);
    }
    
    private void openExplorer(String ID) {
        binding.scanButton.setText(R.string.start_scan);
        
        Intent intent = new Intent(getContext(), FileExplorerActivity.class);
        intent.putExtra(FileExplorerActivity.CONNECTION_ID, ID);
        startActivity(intent);
    }
    
    public static class ConnectViewModel extends ViewModel {
        
        private CancellationSignal cancellationSignal;
        private MainActivity activity;
        private MutableLiveData<NetworkFileSource> source;
        private ScanFragment fragment;
        private SharingDevice device;
        
        private static final String CONNECT_DIALOG = "connectDialog";
            
        public void connectAsync(String password) {
            connect(password, false);
        }
        
        public void cancel() {
            cancellationSignal.cancel();
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
        
        private void connect(String password, boolean firstTry) {
            Bundle bundle = new Bundle();
            bundle.putString(ConnectingDialog.NAME, device.getName());
        
            ConnectingDialog dialog = new ConnectingDialog();
            dialog.setArguments(bundle);
            dialog.show(activity.getSupportFragmentManager(), CONNECT_DIALOG);
  
            ConnectProperties properties = new ConnectProperties(activity.getApplicationContext(),
                Settings.getId(activity), Settings.getDefaultName(activity))
                .setPassword(password)
                .setTimeout(Settings.getConnectTimeout(activity) * 1000)
                .setOnConnectionFailedListener(e -> handleException(e, firstTry, dialog))
                .setOnConnectedListener(source -> {
                    Log.debug.log(TAG, "server id " + source.getServerId());
                    this.source.postValue(source);  
                    cleanUp(dialog);
                });
                
            cancellationSignal = device.openConnection(properties);
        }
        
        private void handleException(Exception exception, boolean firstTry, ConnectingDialog dialog) {
            try {
                throw exception;
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
                Log.err.log(TAG, e);
                
                if(e.getCause() != null && e.getCause().getClass() != InterruptedIOException.class)
                    new ExceptionDialog(e)
                        .show(activity.getSupportFragmentManager(), null);
            } finally {
                cleanUp(dialog);
            }
        }
        
        private void cleanUp(ConnectingDialog dialog) {
            Fragment fragment = activity.getSupportFragmentManager()
                    .findFragmentByTag(CONNECT_DIALOG);
            if(!(fragment instanceof DialogFragment)) {
                dialog.dismiss();
                return;
            }
                
            dialog = (ConnectingDialog) fragment;
            dialog.dismiss();
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
        }
    
    }
    
}
