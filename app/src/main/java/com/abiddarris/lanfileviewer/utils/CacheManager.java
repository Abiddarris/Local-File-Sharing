package com.abiddarris.lanfileviewer.utils;

import com.gretta.util.log.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class CacheManager<K,V> {
    
    private volatile boolean loaded;
    private volatile boolean loading;
    private volatile Map<K,V> caches; 
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    
    protected final String TAG = Log.getTag(getClass());
    
    public final V get(K key) {
        if(!loaded) {
            load();
        }
      
        while(loading) {}
        
        Timer timer = new Timer();
        V cache = caches.get(key);
        if(cache != null) {
            boolean valid = validate(key, cache);
            if(valid) {
                Log.debug.log(TAG, "returning from cache with time " + timer.reset() + " ms");
                return cache;
            }
            Log.debug.log(TAG, "Cache invalid");
            caches.remove(key);
        }
        
        Log.debug.log(TAG, "Checking cache takes " + timer.reset() + " ms");
        
        V value = create(key);
        if(value == null) {
            throw new IllegalArgumentException("create() cannot return null");
        }    
        caches.put(key, value);
        return value;
    }
    
    public final boolean isLoaded() {
        return loaded;
    }
    
    public final synchronized void load() {
        if(loaded) {
            return;
        }
        loaded = true;
        loading = true;
            
        caches = onLoad();
        loading = false;
            
        if(caches == null) {
            throw new IllegalArgumentException("onLoad() cannot return null");
        }    
            
        scheduleSave(executor, new BaseRunnable((c) -> save()));
    }
    
    public final void save() throws Exception{
        onSave(caches);
    }
    
    public void clear() {}
    
    protected abstract V create(K key);
    
    protected abstract boolean validate(K key, V value);
    
    protected Map<K,V> onLoad() {
        return new HashMap<>();
    }
    
    protected void onSave(Map<K,V> caches) throws Exception {}
    
    protected void scheduleSave(ScheduledExecutorService executor, BaseRunnable runnable) {
        Log.debug.log(TAG, "scheduling...");
        executor.scheduleWithFixedDelay(runnable, 0, 1, TimeUnit.MINUTES);
    }
    
}
