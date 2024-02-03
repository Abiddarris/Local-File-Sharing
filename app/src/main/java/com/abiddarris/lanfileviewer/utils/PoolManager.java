package com.abiddarris.lanfileviewer.utils;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class PoolManager<K,V> {

    private boolean saveStackTrace;
    private Map<K, List<V>> pools = new HashMap<>();
    private List<V> activeObjects = new ArrayList<>();
    private List<K> oneValueOnlyKeys = new ArrayList<>();
    private Map<V, StackTraceElement[]> stackTraces = new HashMap<>();

    public void setSaveStackTrace(boolean saveStackTrace) {
        this.saveStackTrace = saveStackTrace;
    }

    public boolean isSaveStackTrace() {
        return saveStackTrace;
    }

    protected V get(K key) {
        List<V> cachedObjects = getCaches(key);

        synchronized(this) { 
            V candidate = findCandidate(cachedObjects, key);

            if(candidate == null) {
                candidate = create(key);
                cachedObjects.add(candidate);
            }

            if(!isOneOnlyValue(key))
                addToActiveCache(key, candidate);

            return candidate;
        }
    }

    public synchronized void free(V value) {
        activeObjects.remove(value);
        stackTraces.remove(value);
    }

    protected synchronized void registerToCache(K key, V value) {
        List<V> cachedObjects = getCaches(key);
        cachedObjects.add(value);
    }

    public V[] getActiveObjects(V[] array) {
        return activeObjects.toArray(array);
    }

    public StackTraceElement[] getStackTraces(V value) {    
        return stackTraces.get(value);
    }

    public synchronized void release() {
        pools.clear();
        activeObjects.clear();
        stackTraces.clear();
        oneValueOnlyKeys.clear();
    }

    public void setOneValueOnly(K key) {
        oneValueOnlyKeys.add(key);
    }

    public boolean isOneOnlyValue(K key) {
        return oneValueOnlyKeys.contains(key);
    }

    private synchronized List<V> getCaches(K key) {
        List<V> cachedObjects = pools.get(key);
        if(cachedObjects == null) {
            cachedObjects = new ArrayList<>();
            pools.put(key,cachedObjects);
        }
        return cachedObjects;
    }

    private V findCandidate(List<V> cachedObjects, K key) {
        for (V value : cachedObjects) {
            V candidate = isOneOnlyValue(key) ?  value : (activeObjects.contains(value) ? null : value); 
            if (candidate != null) return candidate;
        }   
        return null;
    }

    private void addToActiveCache(K key, V value) {      
        activeObjects.add(value);
        if(!saveStackTrace || isOneOnlyValue(key)) {
            return;
        }

        Throwable throwable = new Throwable();
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        stackTraces.put(value, Arrays.copyOfRange(stackTrace, 2, stackTrace.length));
    }


    protected abstract V create(K key);

    
}
