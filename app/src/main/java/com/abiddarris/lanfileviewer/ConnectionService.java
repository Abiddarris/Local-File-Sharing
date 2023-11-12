package com.abiddarris.lanfileviewer;

import android.app.Service;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;

import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import com.abiddarris.lanfileviewer.file.network.NetworkFileServer;
import com.abiddarris.lanfileviewer.file.sharing.FileSharing;
import com.abiddarris.lanfileviewer.file.sharing.SharingSession;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionService extends Service  {

    private boolean isRegistered;
    private boolean isScanning;
    private ConnectionServiceBridge bridge = new ConnectionServiceBridge();
    private DiscoveryListenerImpl discoveryListener = new DiscoveryListenerImpl();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private NsdManager nsdManager;
    private NsdServiceInfo info;
    private ServerListAdapter adapter;
    private SharingSession sharingSession;
    private NetworkFileServer serverThread;

    private static final String SERVICE_TYPE = "_http._tcp.";
    private static final String TAG = Log.getTag(ConnectionService.class);

    @Override
    public IBinder onBind(Intent intent) {
        Log.debug.log(TAG, "Service binded");
        return bridge;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.debug.log(TAG, "Service Created");

        nsdManager = (NsdManager) getSystemService(NSD_SERVICE);
        adapter = new ServerListAdapter(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.debug.log(TAG, "Service Destroy");

        stopScanServer();
        unregisterServer();
    }

    public ServerListAdapter getAdapter() {
        return adapter;
    }


    public boolean isRegistered() {
        return sharingSession != null && sharingSession.isRegistered();
    }

    public void registerServer() {
        if (sharingSession != null) return;
        
        Log.debug.log(TAG, "Registering Server");
        
        sharingSession = FileSharing.share(this);
        sharingSession.start();
    }

    public void unregisterServer() {
        if (sharingSession == null) return;

        Log.debug.log(TAG, "Unregistering Server");
        
        sharingSession.close();
        sharingSession = null;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void scanServer() {
        if (isScanning) return;

        Log.debug.log(TAG, "Scanning for servers");

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        isScanning = true;
    }

    public void stopScanServer() {
        if (!isScanning) return;

        Log.debug.log(TAG, "Scanning Stopped");

        nsdManager.stopServiceDiscovery(discoveryListener);
        adapter.clear();
        
        isScanning = false;
    }

    
    public class DiscoveryListenerImpl implements DiscoveryListener {
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
            Log.err.log(TAG, "Failed to Start Scanning with error code : " + code);
        }

        @Override
        public void onStopDiscoveryFailed(String serverType, int code) {
            Log.err.log(TAG, "Failed to Stop Scanning with error code : " + code);
        }

        @Override
        public void onServiceFound(NsdServiceInfo info) {
            if (!info.getServiceType().equals(SERVICE_TYPE)) return;
            Log.debug.log(TAG, "Server Found! its info : ");
            Log.debug.log(TAG, info.getServiceName());
            Log.debug.log(TAG, info.getServiceType());
            Log.debug.log(TAG, info.getPort());

            nsdManager.resolveService(info, new ResolveListenerImpl());
        }

        @Override
        public void onServiceLost(NsdServiceInfo info) {
            if (!info.getServiceType().equals(SERVICE_TYPE)) return;

            Log.debug.log(TAG, "Server Lost! its info : ");
            Log.debug.log(TAG, info.getServiceName());
            Log.debug.log(TAG, info.getServiceType());
            Log.debug.log(TAG, info.getPort());
            
            handler.post(() -> adapter.removeServer(info));
        }
    }

    public class ResolveListenerImpl implements ResolveListener {
        @Override
        public void onServiceResolved(NsdServiceInfo info) {
            Log.debug.log(TAG, "Server Resolved! its info : ");
            Log.debug.log(TAG, info.getServiceName());
            Log.debug.log(TAG, info.getServiceType());
            Log.debug.log(TAG, info.getPort());

            handler.post(() -> adapter.addServer(info));
        }

        @Override
        public void onResolveFailed(NsdServiceInfo info, int code) {
            Log.debug.log(TAG, "Server Failed to Resolve with code ; " + code + " its info : ");
            Log.debug.log(TAG, info.getServiceName());
            Log.debug.log(TAG, info.getServiceType());
            Log.debug.log(TAG, info.getPort());
        }
    }

    public class ConnectionServiceBridge extends Binder {

        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }
}
