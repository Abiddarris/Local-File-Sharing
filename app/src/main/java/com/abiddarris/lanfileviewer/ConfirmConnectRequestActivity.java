package com.abiddarris.lanfileviewer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.abiddarris.lanfileviewer.databinding.ActivityConfirmConnectRequestBinding;

public class ConfirmConnectRequestActivity extends AppCompatActivity {
   
    private ActivityConfirmConnectRequestBinding binding;
    
    public static final String CLIENT_NAME = "clientName";
    public static final String CLIENT_ID = "clientId";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        
        binding = ActivityConfirmConnectRequestBinding.inflate(getLayoutInflater());
        binding.message.setText(String.format(
            getString(R.string.confirm_connect_request_activity_desc),
            extras.getString(CLIENT_NAME), extras.getString(CLIENT_ID)
        ));
        
        setContentView(binding.getRoot());
    }
    
    
}
