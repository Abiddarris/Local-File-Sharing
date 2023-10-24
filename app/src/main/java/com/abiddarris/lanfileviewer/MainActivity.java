package com.abiddarris.lanfileviewer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import com.abiddarris.lanfileviewer.databinding.ActivityMainBinding;
import com.abiddarris.lanfileviewer.ConnectionService.ConnectionServiceBridge;
import com.google.android.material.tabs.TabLayoutMediator;
import com.gretta.util.log.FilesLog;
import com.gretta.util.log.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ServiceConnection {

    private ActivityMainBinding binding;
    private ConnectionService bridge;
    private List<OnServiceConnected> connectedListeners = new ArrayList<>();

    private static final String TAG = Log.getTag(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.viewPager.setAdapter(new MainFragmentAdapter(this));
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (t,pos) -> {
            switch(pos) {
                case 0 : 
                    t.setText(getString(R.string.home));
                    break;
                case 1 :
                    t.setText(getString(R.string.scan));
                    break;
                case 2 :
                    t.setText(getString(R.string.settings));     
            }
        }).attach();
        
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        Intent intent = new Intent(this, ConnectionService.class);
        startService(intent);
        bindService(intent, this, 0);
    }
    
    public void addConnectedListener(OnServiceConnected listener) {
        if(bridge != null) {
            listener.onConnected(bridge);
            return;
        }
        connectedListeners.add(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;

        Log.debug.log(TAG, "Destroying MainActivity");

        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName component, IBinder bridge) {
        Log.debug.log(TAG, "Connected to ConnectionService");
        this.bridge = ((ConnectionServiceBridge) bridge).getService();

        for(OnServiceConnected connectedListener : connectedListeners) {
            connectedListener.onConnected(this.bridge);
        }
        connectedListeners = null;
    }

    @Override
    public void onServiceDisconnected(ComponentName component) {}

    public static interface OnServiceConnected {
        void onConnected(ConnectionService service);
    }
}