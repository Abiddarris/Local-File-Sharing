package com.abiddarris.lanfileviewer.file.sharing;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import com.gretta.util.log.Log;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanningSession implements DiscoveryListener{
    
    private boolean isScanning;
    private Callback callback;
    private Context context;
    private Map<String,SharingDevice> devices = new HashMap<>();
    private NsdManager nsdManager;
    
    private static final String TAG = Log.getTag(ScanningSession.class);
    
    ScanningSession(Context context, Callback callback) {
        this.context = context;
        this.callback = callback == null ? new CallbackStub() : callback;
        
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }
    
    public void start() {
        nsdManager.discoverServices(
                FileSharing.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this);
        isScanning = true;
    }
    
    public void stop() {
    	nsdManager.stopServiceDiscovery(this);
        
        isScanning = false;
        devices.clear();
    }
    
    public boolean isScanning() {
        return isScanning;
    }
    
    @Override
    public void onDiscoveryStarted(String serverType) {
        Log.debug.log(TAG, "Success Starting Scan");
    }

    @Override
    public void onDiscoveryStopped(String serverType) {
        Log.debug.log(TAG, "Success Stopping Scan");
    }

    @Override
    public void onStartDiscoveryFailed(String serverType, int code) {
        callback.onError(new ScanException("Failed to Start Scanning with error code : " + code));
    }
    
    @Override
    public void onStopDiscoveryFailed(String serverType, int code) {
        callback.onError(new ScanException("Failed to Stop Scanning with error code : " + code));
    }

    @Override
    public void onServiceFound(NsdServiceInfo info) {
        if (!info.getServiceType().equals(FileSharing.SERVICE_TYPE)) return;
       
        Log.debug.log(TAG, "Server Found! its info : ");
        Log.debug.log(TAG, info.getServiceName());
        Log.debug.log(TAG, info.getServiceType());
        Log.debug.log(TAG, info.getPort());

        nsdManager.resolveService(info, new ResolveListenerImpl());
    }

    @Override
    public void onServiceLost(NsdServiceInfo info) {
        if (!info.getServiceType().equals(FileSharing.SERVICE_TYPE)) return;

        Log.debug.log(TAG, "Server Lost! its info : ");
        Log.debug.log(TAG, info.getServiceName());
        Log.debug.log(TAG, info.getServiceType());
        Log.debug.log(TAG, info.getPort());
            
        SharingDevice device = devices.remove(info.getServiceName());
        callback.onServerLost(device);
    }
    
    
    public class ResolveListenerImpl implements ResolveListener {
        @Override
        public void onServiceResolved(NsdServiceInfo info) {
            Log.debug.log(TAG, "Server Resolved! its info : ");
            Log.debug.log(TAG, info.getServiceName());
            Log.debug.log(TAG, info.getServiceType());
            Log.debug.log(TAG, info.getPort());
            
            SharingDevice device = new SharingDevice(info);
            devices.put(info.getServiceName(), device);
            
            callback.onServerFound(device);
        }

        @Override
        public void onResolveFailed(NsdServiceInfo info, int code) {
            callback.onError(new ScanException(
                "Server Failed to Resolve with code : " + code +
                " its info : \n " + info.getServiceName() + "\n " +
                info.getServiceType() + "\n" + info.getPort()
            ));
        }
    }
    
    public static interface Callback {
        
        void onError(ScanException exception);
        
        void onServerFound(SharingDevice device);
        
        void onServerLost(SharingDevice device);
        
    }
    
    private class CallbackStub implements Callback {
    
        @Override
        public void onError(ScanException exception) {
        }
        

        @Override
        public void onServerFound(SharingDevice device) {
        }
        

        @Override
        public void onServerLost(SharingDevice device) {
        }
        
    }
}
