package com.abiddarris.lanfileviewer.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.utils.Theme;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences,rootKey);
        
        final ListPreference theme = findPreference("theme");
        final ListPreference writeAccess = findPreference("writeAccess");
        final ListPreference deleteAccess = findPreference("deleteAccess");
        EditTextPreference name = findPreference("name");
        EditTextPreference password = findPreference("password");
        EditTextPreference connectTimeout = findPreference("connect_timeout");
        
        name.setDefaultValue(Settings.getDefaultName(getContext()));
        name.setSummaryProvider(p -> Settings.getDefaultName(getContext()));
        name.setOnBindEditTextListener((e) -> {
             e.setText(Settings.getDefaultName(getContext()));
             e.setSelection(e.getText().length());
        });
        name.setOnPreferenceChangeListener((p,newName) -> !((String)newName).isBlank());
        
        theme.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        writeAccess.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        deleteAccess.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        password.setSummaryProvider(p -> {
            String passwordValue = Settings.getPassword(getContext());
            return passwordValue == null ? getString(R.string.no_password) : passwordValue; 
        });
        connectTimeout.setOnBindEditTextListener(e -> e.setInputType(InputType.TYPE_CLASS_NUMBER));
    }
    
    @Override
    public void onDisplayPreferenceDialog(Preference pref) {
        if(pref instanceof CustomPreference) {
            DialogFragment dialog = CustomPreference.createFrom(pref);
            dialog.setTargetFragment(this,0);
            dialog.show(getActivity().getSupportFragmentManager(), null);
            return;
        }    
        super.onDisplayPreferenceDialog(pref);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        if(key.equals("theme")) {
            Theme.apply(getContext());
        }
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onResume() {
        super.onResume();
        
        getPreferenceManager().getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    }
    
    
}
