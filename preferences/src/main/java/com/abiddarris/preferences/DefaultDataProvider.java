package com.abiddarris.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class DefaultDataProvider implements DataProvider {
    
    private SharedPreferences preferences;
    
    public DefaultDataProvider(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    @Override
    public Object getValue(String key) {
        return preferences.getAll()
            .get(key);
    }
    
}
