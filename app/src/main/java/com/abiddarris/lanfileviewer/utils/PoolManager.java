package com.abiddarris.lanfileviewer.utils;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class PoolManager<K,V extends Poolable> {
    
    public static enum Policy {
        MULTIPLE_REFERENCE, SINGLE_REFERENCE
    }

    private boolean saveStackTrace;
    private Map<K, KeyInfo> pools = new HashMap<>();
    private List<V> activeObjects = new ArrayList<>();
    private Map<V, StackTraceElement[]> stackTraces = new HashMap<>();

    public void setSaveStackTrace(boolean saveStackTrace) {
        this.saveStackTrace = saveStackTrace;
    }

    public boolean isSaveStackTrace() {
        return saveStackTrace;
    }

    protected V get(K key) {
        KeyInfo info = getKeyInfo(key);

        synchronized(this) { 
            V candidate = findCandidate(info.values, key);

            if(candidate == null) {
                candidate = create(key);
                info.values.add(candidate);
            }

            if(!isMultipleReference(key))
                addToActiveCache(key, candidate);

            return candidate;
        }
    }

    public synchronized void free(V value) {
        activeObjects.remove(value);
             
        stackTraces.remove(value);
        value.onFreed();
    }

    protected synchronized void registerToCache(K key, V value) {
        getKeyInfo(key).values
            .add(value);
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
    }

    public void setPolicies(K key, Policy... policies) {
        getKeyInfo(key)
            .setPolicies(policies);
    }
    
    public int getCacheSize() {
        int size = 0;
        for(KeyInfo info : pools.values()) {
            size += info.values.size();
        }
        return size;
    }

    private boolean isMultipleReference(K key) {
        return getKeyInfo(key)
            .hasPolicy(Policy.MULTIPLE_REFERENCE);
    }

    private synchronized KeyInfo getKeyInfo(K key) {
        KeyInfo info = pools.get(key);
        if(info == null) {
            info = new KeyInfo();
            pools.put(key, info);
        }
        return info;
    }

    private V findCandidate(List<V> cachedObjects, K key) {
        for (V value : cachedObjects) {
            V candidate = isMultipleReference(key) ?  value : (activeObjects.contains(value) ? null : value); 
            if (candidate != null) return candidate;
        }   
        return null;
    }

    private void addToActiveCache(K key, V value) {      
        activeObjects.add(value);
        value.onPooled();
        if(!saveStackTrace || isMultipleReference(key)) {
            return;
        }

        Throwable throwable = new Throwable();
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        stackTraces.put(value, Arrays.copyOfRange(stackTrace, 2, stackTrace.length));
    }

    protected abstract V create(K key);

    private class KeyInfo {
        private List<V> values = new ArrayList<>();             
        private Policy[] policies = {Policy.SINGLE_REFERENCE};     
        
        private void setPolicies(Policy... policies) {
            this.policies = policies;
        }
        
        private boolean hasPolicy(Policy policy) {
            for(Policy _policy : policies) {
                if(_policy == policy) 
                    return true;
            }
            return false;
        }
    }
}
