package com.abiddarris.lanfileviewer.ui;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;

import com.abiddarris.lanfileviewer.ConnectionService;
import com.abiddarris.lanfileviewer.FileExplorerActivity;
import com.abiddarris.lanfileviewer.MainActivity;
import com.abiddarris.lanfileviewer.databinding.FragmentScanBinding;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice;

public class ScanFragment extends Fragment {
    
    private ConnectionService service;
    private FragmentScanBinding binding;
    
    public ScanFragment() {
        super(R.layout.fragment_scan);
    }
    
    @Override
    @MainThread
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        
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
        
        binding.sharingDevices.setOnItemClickListener((adapterView,v,index,i) -> {
            SharingDevice info = (SharingDevice)adapterView.getItemAtPosition(index);
       
            binding.scanButton.setText(R.string.start_scan);
        
            Intent intent = new Intent(getContext(), FileExplorerActivity.class);
            intent.putExtra(FileExplorerActivity.SERVER_NAME, info.getName());
            startActivity(intent);
        });
        
        MainActivity activity = (MainActivity)getActivity();
        activity.addConnectedListener((bridge) -> {
            service = bridge;    
            binding.sharingDevices.setAdapter(service.getAdapter());
        });
    }
    
    
}
