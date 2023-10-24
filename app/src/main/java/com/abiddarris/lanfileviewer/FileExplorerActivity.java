package com.abiddarris.lanfileviewer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.abiddarris.lanfileviewer.ConnectionService.ConnectionServiceBridge;
import com.abiddarris.lanfileviewer.databinding.ActivityFileExplorerBinding;
import com.abiddarris.lanfileviewer.explorer.BaseExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.NetworkExplorerFragment;
import com.abiddarris.lanfileviewer.file.network.NetworkFile;
import com.abiddarris.lanfileviewer.file.network.NetworkFileClient;
import com.gretta.util.log.Log;

public class FileExplorerActivity extends AppCompatActivity
        implements ServiceConnection {

    private ActivityFileExplorerBinding binding;
    private NetworkFileClient clientThread;
    private ConnectionService bridge;
    
    private static final String TAG = Log.getTag(FileExplorerActivity.class);
    public static final String SERVER_NAME = "serverName";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        binding = ActivityFileExplorerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        bindService(new Intent(this, ConnectionService.class), this, 0);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().setFragmentResult(BaseExplorerFragment.BACK_PRESSED_EVENT, new Bundle());
        return true;
    }
    

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.debug.log(TAG, "Destroying Explorer Activity");

        clientThread.close();
    }

    @Override
    public void onServiceConnected(ComponentName component, IBinder binder) {
        Log.debug.log(TAG, "Connected to ConnectionService");

        String name = getIntent().getStringExtra(SERVER_NAME);

        bridge = ((ConnectionServiceBridge) binder).getService();
        Log.debug.log(TAG, "Finding server with name : " + name);
        NsdServiceInfo info = bridge.getAdapter().getServer(name);

        clientThread = new NetworkFileClient(info.getHost(), info.getPort());
        clientThread.setConnectedCallback(files -> {
            getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragmentContainer, NetworkExplorerFragment.class, null)
            .commit();
        });
        
        ApplicationCore app = (ApplicationCore)getApplicationContext();
        app.setNetworkFileClient(clientThread);
    }

    @Override
    public void onServiceDisconnected(ComponentName component) {}
}
