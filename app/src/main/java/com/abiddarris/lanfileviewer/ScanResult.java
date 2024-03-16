package com.abiddarris.lanfileviewer;

import android.os.Handler;
import android.os.Looper;

import com.abiddarris.lanfileviewer.file.sharing.ScanException;
import com.abiddarris.lanfileviewer.file.sharing.ScanningSession;
import com.abiddarris.lanfileviewer.file.sharing.SharingDevice;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.gretta.util.log.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScanResult implements ScanningSession.Callback {

    public static final String TAG = Log.getTag(ScanResult.class);

    private Set<SharingDevice> results = new HashSet<>();
    private Set<OnResultUpdatedListener> updatedListeners = new HashSet<>();
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    
    @Override
    public void onError(ScanException exception) {
        Log.err.log(TAG, exception);
    }

    @Override
    public void onServerFound(SharingDevice device) {
        handler.post((c) -> {
            results.add(device);
            sendUpdateListener(Event.FOUND, device)  ;  
        });
    }

    @Override
    public void onServerLost(SharingDevice device) {
        handler.post((c) -> {
            results.remove(device);
            sendUpdateListener(Event.LOST, device); 
        });
    }
    
    public List<SharingDevice> getResults() {
        return new ArrayList<>(this.results);
    }
    
    public void addUpdatedListener(OnResultUpdatedListener listener) {
        updatedListeners.add(listener);
    }
    
    public void removeUpdatedListener(OnResultUpdatedListener listener) {
        updatedListeners.remove(listener);
    }
    
    public static interface OnResultUpdatedListener {
        void onResultUpdated(ScanResult result, Event event, SharingDevice device);
    }
    
    public static enum Event {
        FOUND, LOST
    }
    
    public SharingDevice getServer(String name) {
        for(SharingDevice device : results) {
            if(device.getName().equals(name)) {
                return device;
            }
        }
        return null;
    }

    void clear() {
        for(SharingDevice result : results) {
            onServerLost(result);
        } 
    }
     
    private void sendUpdateListener(Event event, SharingDevice device) {
        for(OnResultUpdatedListener listener : updatedListeners) {
            listener.onResultUpdated(this, event, device);
        }
    }
    
    
}
