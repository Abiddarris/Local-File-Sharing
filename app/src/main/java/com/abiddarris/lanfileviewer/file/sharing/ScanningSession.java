package com.abiddarris.lanfileviewer.file.sharing;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;

import com.gretta.util.log.Log;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ScanningSession implements DiscoveryListener{
    
    private boolean isScanning;
    private boolean queueLocked;
    private Callback callback;
    private Context context;
    private Map<String,SharingDevice> devices = new HashMap<>();
    private Queue<NsdServiceInfo> resolveQueue = new ArrayDeque<>();
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
    
    private void releaseQueueLock() {
        resolveQueue.poll();
        queueLocked = false;
        runQueue();
    }
    
    private void runQueue() {
        if(queueLocked) return;
        
        NsdServiceInfo service = resolveQueue.peek();
        if(service == null) return;
        
        nsdManager.resolveService(service, new ResolveListenerImpl());
        queueLocked = true;
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

        resolveQueue.add(info);
        runQueue();
    }

    @Override
    public void onServiceLost(NsdServiceInfo info) {
        if (!info.getServiceType().equals(FileSharing.SERVICE_TYPE)) return;

        Log.debug.log(TAG, "Server Lost! its info : ");
        Log.debug.log(TAG, info.getServiceName());
        Log.debug.log(TAG, info.getServiceType());
        Log.debug.log(TAG, info.getPort());
        
        resolveQueue.remove(info);
        
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
            Log.debug.log(TAG, info.getHost().getHostAddress());
            
            SharingDevice device = new SharingDevice(info);
            if(devices.containsValue(device)) return;
            
            devices.put(info.getServiceName(), device);
            
            callback.onServerFound(device);
            releaseQueueLock();
        }

        @Override
        public void onResolveFailed(NsdServiceInfo info, int code) {
            callback.onError(new ScanException(
                "Server Failed to Resolve code : " + code +
                ", name :" + info.getServiceName() + ", type : " +
                info.getServiceType() + ", port : " + info.getPort()
            ));
            releaseQueueLock();
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
