package com.abiddarris.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleEventObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferenceChangeDelegator
        implements LifecycleEventObserver, OnSharedPreferenceChangeListener {

    private Map<String, List<PreferenceKeyChangedListener>> listeners = new HashMap<>();
    private SharedPreferences preferences;

    public PreferenceChangeDelegator(SharedPreferences preferences) {
        this.preferences = preferences;
    }
    
    public void addListener(String key, PreferenceKeyChangedListener listener) {
    	getListeners(key)
            .add(listener);
    }
    
    public void removeListener(String key, PreferenceKeyChangedListener listener) {
    	getListeners(key)
            .remove(listener);
    }
    
    private synchronized List<PreferenceKeyChangedListener> getListeners(String key) {
        List<PreferenceKeyChangedListener> listeners = this.listeners.get(key);
        if(listeners == null) {
            listeners = new ArrayList<>();
            this.listeners.put(key, listeners);
        }
        return listeners;
    }

    @Override
    public void onStateChanged(LifecycleOwner owner, Event event) {
        switch (event) {
            case ON_RESUME:
                preferences.registerOnSharedPreferenceChangeListener(this);
                break;
            case ON_PAUSE :
                preferences.unregisterOnSharedPreferenceChangeListener(this);
        }    
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        List<PreferenceKeyChangedListener> listeners = this.listeners.get(key);
        for(PreferenceKeyChangedListener listener : listeners) {
        	listener.onPreferenceKeyChanged(sharedPreferences, key);
        }
    }
}
