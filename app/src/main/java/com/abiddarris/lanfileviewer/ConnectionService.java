package com.abiddarris.lanfileviewer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.gretta.util.Randoms;
import com.gretta.util.log.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionService extends Service implements ScanningSession.Callback {
    
    private CancelNotificationReceiver cancelNotificationReceiver = new CancelNotificationReceiver();
    private ConnectionServiceBridge bridge = new ConnectionServiceBridge();
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    private Map<Integer, Lock> locks = new HashMap<>();
    private NotificationManager manager;
    private Random random = new Random();
    private ServerListAdapter adapter;
    private ScanningSession session;
    private SharingSession sharingSession;
    
    private static final String NOTIFICATION_CHANNEL_ID = "mainNotification";
    private static final String CONFIRM_CONNECT_REQUEST = "confirmConnectRequest";
    private static final String SERVICE_TYPE = "_http._tcp.";
    private static final String TAG = Log.getTag(ConnectionService.class);
    private static final String CANCEL_NOTIFICATION = "cancel_notification";
    private static final String NOTIFICATION_ID = "notificationId";
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.debug.log(TAG, "Service binded");
        return bridge;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.debug.log(TAG, "Service Created");
        
        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        createNotificationChannel();
        
        registerReceiver(cancelNotificationReceiver, new IntentFilter(CANCEL_NOTIFICATION), RECEIVER_NOT_EXPORTED);
        
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
        
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(cancelNotificationReceiver);
        
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
        
        channel = new NotificationChannelCompat.Builder(CONFIRM_CONNECT_REQUEST, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(getString(R.string.confirm_connect_request_channel))
            .setDescription(getString(R.string.confirm_connect_request_channel_desc))
            .build();
        
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
        
        List<java.io.File> roots = Settings.getRoots(this);
        FileSource source = new LocalFileSource(this, roots.toArray(new java.io.File[0]));
        source.setSecurityManager(new SecurityManagerImpl());
        
        String name = Settings.getDefaultName(this);
        
        sharingSession = FileSharing.share(this, source);
        sharingSession.setPassword(Settings.getPassword(this));
        sharingSession.setConnectListener(client -> {
            if(!Settings.isConfirmConnectRequest(this)) return true;
            
            int id = random.nextInt();
                
            String title = getString(R.string.confirm_connect_request_notification_title);
            Intent intent = new Intent(this, ConfirmConnectRequestActivity
                    .class);
            intent.putExtra(ConfirmConnectRequestActivity.CLIENT_NAME, client.getClient());
            intent.putExtra(ConfirmConnectRequestActivity.CLIENT_ID, client.getClientId());
            intent.putExtra(ConfirmConnectRequestActivity.REQUEST_ID, id);
                
            Intent deleteIntent = new Intent(CANCEL_NOTIFICATION);
            deleteIntent.putExtra(NOTIFICATION_ID, id);
                
            PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_IMMUTABLE); 
            PendingIntent deletePendingIntent = PendingIntent.getBroadcast(this, id, deleteIntent, PendingIntent.FLAG_IMMUTABLE); 
                
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CONFIRM_CONNECT_REQUEST)
                .setSmallIcon(R.drawable.icons8_folder)
                .setContentTitle(String.format(title, client.getClient()))
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true);
            
            Lock lock = new Lock(); 
            lock.notificationCountdown = new NotificationCountdown(
                builder, client.getClientTimeout() / 1000, id);
            locks.put(id, lock);
                
            handler.post(lock.notificationCountdown);
                
            try {
                synchronized(lock) {
                    lock.wait();
                }
            } catch(InterruptedException e) {
            	Log.err.log(TAG, e);
            }    
            
            return lock.accept;
        });
        try {
            sharingSession.start(name);
        } catch (Exception e) {
            Log.err.log(TAG,e);
        }
    }
    
    public void acceptConnection(int id, boolean accept) {
    	Lock lock = locks.remove(id);
        if(lock == null) return;
        
        synchronized(lock) {
            lock.accept = accept;
            lock.notifyAll();
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
    
    private class CancelNotificationReceiver extends BroadcastReceiver {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!CANCEL_NOTIFICATION.equals(intent.getAction())) return;
            
            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            locks.get(id)
                .notificationCountdown
                .stop();
            
            acceptConnection(id, false);
            
            manager.cancel(id);
        }
        
    }
    
    private class NotificationCountdown extends BaseRunnable {
        
        private NotificationCompat.Builder builder;
        private long countDown;
        private int id;
        private String text = getString(R.string.confirm_connect_request_notification_desc);
        private String second = getString(R.string.second);
        
        public NotificationCountdown(NotificationCompat.Builder builder, long countDown, int id) {
            this.builder = builder;
            this.countDown = countDown;
            this.id = id;
        }
        
        @Override
        public void onExecute(BaseRunnable context) throws Exception {
            super.onExecute(context);
            
            if(countDown == 0) {
                manager.cancel(id);
                return;
            }
            
            builder.setContentText(String.format(text,countDown,second));
            
            manager.notify(id, builder.build());
            
            countDown--;
          
            handler.postDelayed(this, 1000);
        }
        
        public void stop() {
            countDown = 0;
        }
        
    }
    
    private class Lock {
        private boolean accept;
        private NotificationCountdown notificationCountdown;
    }
}
