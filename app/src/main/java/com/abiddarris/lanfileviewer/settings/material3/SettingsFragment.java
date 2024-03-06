package com.abiddarris.lanfileviewer.settings.material3;

import com.abiddarris.preferences.EditTextPreference;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.preferences.ListEntry;
import com.abiddarris.preferences.ListPreference;
import com.abiddarris.preferences.Preference;
import com.abiddarris.preferences.PreferenceCategory;
import com.abiddarris.preferences.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public Preference[] onCreatePreference() {
        EditTextPreference name = new EditTextPreference(this, "name");
        name.setTitle(R.string.name);
        name.setSummaryProvider(EditTextPreference.EditTextSummaryProvider.getInstance());
        
        EditTextPreference password = new EditTextPreference(this, "password");
        password.setTitle(R.string.password);
        password.setSummaryProvider(EditTextPreference.EditTextSummaryProvider.getInstance());
        
        EditTextPreference connectTimeout = new EditTextPreference(this, "connect_timeout");
        connectTimeout.setTitle(R.string.timeout);
        connectTimeout.setSummaryProvider(EditTextPreference.EditTextSummaryProvider.getInstance());
        
        ListPreference preference = new ListPreference(this, "theme");
        preference.setTitle(R.string.theme);
        preference.setEntries(
            new ListEntry(getString(R.string.dark), "0"),
            new ListEntry(getString(R.string.light), "1"),
            new ListEntry(getString(R.string.follow_system), "2")
        );
        
        PreferenceCategory general = new PreferenceCategory(this, "general");
        general.setTitle(R.string.general);
        general.addPreference(name, password, connectTimeout, preference);
        
        PreferenceCategory files = new PreferenceCategory(this, "files");
        files.setTitle(R.string.files);
        
        PreferenceCategory permission = new PreferenceCategory(this, "permission");
        permission.setTitle(R.string.permission);
        
        PreferenceCategory cache = new PreferenceCategory(this, "cache");
        cache.setTitle(R.string.cache);
        
        return new Preference[] {general, files, permission, cache};
    }
}
