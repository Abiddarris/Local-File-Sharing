package com.abiddarris.preferences;

import android.content.Context;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class PreferenceCategory extends Preference {

    private Context context;
    private List<Preference> preferences = new ArrayList<>();
    private String category;

    public PreferenceCategory(Context context, String key) {
        super(context, key);
    }
    
    public void addPreference(Preference preference) {
        if(preference instanceof PreferenceCategory) 
            throw new IllegalArgumentException("preference cannot be category!");
        preferences.add(preference);
    }
    
    public Preference[] getPreferences() {
        return preferences.toArray(new Preference[0]);
    }
    
}
