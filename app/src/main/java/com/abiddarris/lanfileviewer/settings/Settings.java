package com.abiddarris.lanfileviewer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.preference.PreferenceManager;

public class Settings {
    
    public static String getDefaultName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("name", Build.BRAND + " " + Build.DEVICE);
    }
    
}
