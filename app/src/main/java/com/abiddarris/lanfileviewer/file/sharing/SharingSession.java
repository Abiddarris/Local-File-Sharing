package com.abiddarris.lanfileviewer.file.sharing;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import com.abiddarris.lanfileviewer.file.network.NetworkFileServer;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.net.nsd.NsdManager.RegistrationListener;

public final class SharingSession implements RegistrationListener {
    
    private boolean isRegistered;
    private Context context;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private NsdManager nsdManager;
    private NetworkFileServer serverThread;
    
    private static final String TAG = Log.getTag(SharingSession
        .class);
    
    public SharingSession(Context context) {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }
    
    public void onPortAvailable(int port) {
        Log.debug.log(TAG, "Port Available : " + port);

        NsdServiceInfo info = new NsdServiceInfo();
        info.setServiceName(Build.BRAND + " " + Build.DEVICE + "_FILEV");
        info.setServiceType(FileSharing.SERVICE_TYPE);
        info.setPort(port);
        
        nsdManager.registerService(info, NsdManager.PROTOCOL_DNS_SD, this);

        isRegistered = true;
    }
    
    public void start() {
        serverThread = new NetworkFileServer(this);
        executor.submit(serverThread);
    }
    
    public void close() {
        serverThread.close();
        serverThread = null;

        nsdManager.unregisterService(this);
        isRegistered = false;
    }
    
    public Context getContext() {
        return context;
    }
    
    public boolean isRegistered() {
        return isRegistered;
    }
    
    @Override
    public void onRegistrationFailed(NsdServiceInfo info, int code) {
        Log.err.log(TAG, "Failed to register server with error code : " + code);
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo info, int code) {
        Log.err.log(TAG, "Failed to unregister server with error code : " + code);
    }

    @Override
    public void onServiceRegistered(NsdServiceInfo info) {
        Log.debug.log(TAG, "Sucess registering service with name " + info.getServiceName());
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo info) {
        Log.debug.log(TAG, "Sucess unregistering service");
    }

}
