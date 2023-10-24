package com.abiddarris.lanfileviewer.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;

import com.abiddarris.lanfileviewer.ConnectionService;
import com.abiddarris.lanfileviewer.MainActivity;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    
    private ConnectionService service;
    private FragmentHomeBinding binding;
    
    public HomeFragment() {
        super(R.layout.fragment_home);
    }
    
    @Override
    @MainThread
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        
        binding = FragmentHomeBinding.bind(view);
        binding.shareButton.setOnClickListener((v) -> {
            if (!service.isRegistered()) {
                binding.shareButton.setText(getString(R.string.stop_share));
                service.registerServer();
                return;
            }
            binding.shareButton.setText(getString(R.string.start_share));
            service.unregisterServer();
        });
        
        MainActivity activity = (MainActivity) getActivity();
        activity.addConnectedListener(bridge -> {
            service = bridge;
            if (service.isRegistered()) {
                binding.shareButton.setText(getString(R.string.stop_share));
            }   
        });
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        
        service = null;
        binding = null;
    }
    
}
