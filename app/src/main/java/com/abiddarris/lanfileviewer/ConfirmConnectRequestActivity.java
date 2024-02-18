package com.abiddarris.lanfileviewer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.abiddarris.lanfileviewer.databinding.ActivityConfirmConnectRequestBinding;

public class ConfirmConnectRequestActivity extends AppCompatActivity {
   
    private ActivityConfirmConnectRequestBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityConfirmConnectRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
    
    
}
