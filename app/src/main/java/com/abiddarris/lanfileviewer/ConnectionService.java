package com.abiddarris.lanfileviewer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.SecurityException;
import com.abiddarris.lanfileviewer.file.SecurityManager;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.abiddarris.lanfileviewer.file.sharing.FileSharing;
import com.abiddarris.lanfileviewer.file.sharing.ScanException;
import com.abiddarris.lanfileviewer.file.sharing.ScanningSession;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice;
import com.abiddarris.lanfileviewer.file.sharing.SharingSession;
import com.abiddarris.lanfileviewer.settings.Settings;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionService extends Service implements ScanningSession.Callback {
    
    private ConnectionServiceBridge bridge = new ConnectionServiceBridge();
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    private ServerListAdapter adapter;
    private ScanningSession session;
    private SharingSession sharingSession;
    
    private static final String NOTIFICATION_CHANNEL_ID = "mainNotification";
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
        
        createNotificationChannel();
        
        Log.debug.log(TAG, adapter);
        
        adapter = new ServerListAdapter(this);
    }
    
    @Override
    public int onStartCommand(Intent arg0, int arg1, int arg2) {
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.icons8_folder)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setPriority(NotificationManagerCompat.IMPORTANCE_MIN)
            .build();
        
        startForeground(1, notification);
        return START_STICKY;
    }
    
    @Override
    public void onTaskRemoved(Intent intent) {
        if(!isRegistered()) {
            Log.debug.log(TAG, "Stopping service");
            stopSelf();
        }
        super.onTaskRemoved(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.debug.log(TAG, "Service Destroy");

        stopScanServer();
        unregisterServer();
    }

    private void createNotificationChannel() {
        NotificationChannelCompat channel = new NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_MIN)
            .setName(getString(R.string.notification_channel_title))
            .setDescription(getString(R.string.notification_channel_description))
            .build();
        
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.createNotificationChannel(channel);
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
        
        FileSource source = LocalFileSource.getDefaultLocalSource(this);
        source.setSecurityManager(new SecurityManagerImpl());
        
        String name = Settings.getDefaultName(this);
        
        sharingSession = FileSharing.share(this, source);
        try {
            sharingSession.start(name);
        } catch (Exception e) {
            Log.err.log(TAG,e);
        }
    }

    public void unregisterServer() {
        if (sharingSession == null) return;

        Log.debug.log(TAG, "Unregistering Server");
        
        sharingSession.close();
        sharingSession = null;
    }

    public boolean isScanning() {
        return session != null && session.isScanning();
    }

    public void scanServer() {
        if (isScanning()) return;

        Log.debug.log(TAG, "Scanning for servers");

        session = FileSharing.scan(this, this);
        session.start();
    }

    public void stopScanServer() {
        if (!isScanning()) return;

        Log.debug.log(TAG, "Scanning Stopped");

        session.stop();
        adapter.clear();
    }
    
    @Override
    public void onError(ScanException exception) {
        Log.err.log(TAG, exception);
    }
    
    @Override
    public void onServerFound(SharingDevice device) {
        handler.post((c) -> adapter.addServer(device));
    }
    
    @Override
    public void onServerLost(SharingDevice device) {
        handler.post((c) -> adapter.removeServer(device));
    }
    
    private class SecurityManagerImpl extends SecurityManager {
        
        private SharedPreferences preferences;
        
        public SecurityManagerImpl() {
        	preferences = PreferenceManager.getDefaultSharedPreferences(ConnectionService.this);
        }
        
        @Override
        public void checkWrite(File file) {
            String granted = preferences.getString("writeAccess", "1");
            
            if(granted.equals("1")) {
                throw new SecurityException("Write access dissalowed");
            }
        }
        
        @Override
        public void checkDelete(File file) {
            String granted = preferences.getString("deleteAccess", "1");
            
            if(granted.equals("1")) {
                throw new SecurityException("Delete access dissalowed");
            }
        }
        
    }

    public class ConnectionServiceBridge extends Binder {

        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }
}
