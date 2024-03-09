package com.abiddarris.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class DefaultDataStore implements DataStore {

    private SharedPreferences preferences;

    public DefaultDataStore(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public String getString(String key) {
        return preferences.getString(key, null);
    }
    
    @Override
    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }
    
    @Override
    public void store(String key, String value) {
        preferences.edit()
            .putString(key, value)
            .commit();
    }
    
    @Override
    public void store(String key, boolean value) {
        preferences.edit()
            .putBoolean(key, value)
            .commit();
    }
    
}
