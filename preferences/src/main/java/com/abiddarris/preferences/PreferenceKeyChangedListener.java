package com.abiddarris.preferences;
import android.content.SharedPreferences;

public interface PreferenceKeyChangedListener {
    void onPreferenceKeyChanged(SharedPreferences preference, String key);
}
