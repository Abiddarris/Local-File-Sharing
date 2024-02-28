package com.abiddarris.lanfileviewer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import com.abiddarris.lanfileviewer.databinding.ActivityConfirmConnectRequestBinding;

public class ConfirmConnectRequestActivity extends AppCompatActivity implements ServiceConnection {
   
    private ActivityConfirmConnectRequestBinding binding;
    private ConnectionService service;
    private int id;
    
    public static final String CLIENT_NAME = "clientName";
    public static final String CLIENT_ID = "clientId";
    public static final String REQUEST_ID = "requestId";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        id = extras.getInt(REQUEST_ID);
        
        binding = ActivityConfirmConnectRequestBinding.inflate(getLayoutInflater());
        binding.message.setText(String.format(
            getString(R.string.confirm_connect_request_activity_desc),
            extras.getString(CLIENT_NAME), extras.getString(CLIENT_ID)
        ));
        binding.confirm.setOnClickListener(v -> {
            if(service == null) return;
            
            service.acceptConnection(id, true);    
                
            finish();
        });
        binding.reject.setOnClickListener(v -> {
            if(service == null) return;
            
            service.acceptConnection(id, false);    
                
            finish();
        });
        
        setContentView(binding.getRoot());
        
        Intent intent = new Intent(this, ConnectionService.class);
        startForegroundService(intent);
        bindService(intent, this, 0);
    }
    
    @Override
    public void onServiceConnected(ComponentName component, IBinder iBinder) {
        service = ((ConnectionService.ConnectionServiceBridge)iBinder)
            .getService();
        service.cancelNotification(id);
    }
    
    @Override
    public void onServiceDisconnected(ComponentName component) {
        service = null;
    }
    
}
