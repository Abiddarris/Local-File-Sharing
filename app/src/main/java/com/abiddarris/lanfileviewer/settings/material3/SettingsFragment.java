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
        
        ListPreference themes = new ListPreference(this, "theme");
        themes.setTitle(R.string.theme);
        themes.setEntries(
            new ListEntry(getString(R.string.dark), "0"),
            new ListEntry(getString(R.string.light), "1"),
            new ListEntry(getString(R.string.follow_system), "2")
        );
        themes.setDefaultValue("0");
        themes.setSummaryProvider(ListPreference.ListPreferenceSummaryProvider.getInstance());
        
        PreferenceCategory general = new PreferenceCategory(this, "general");
        general.setTitle(R.string.general);
        general.addPreference(name, password, connectTimeout, themes);
        
        PreferenceCategory files = new PreferenceCategory(this, "files");
        files.setTitle(R.string.files);
        
        ListEntry[] accessPermissions = {
            new ListEntry(getString(R.string.allow), "0"),
            new ListEntry(getString(R.string.disallow), "1"),
            new ListEntry(getString(R.string.ask), "2")
        };
        
        ListPreference writeAccess = new ListPreference(this, "writeAccess");
        writeAccess.setDefaultValue("1");
        writeAccess.setSummaryProvider(ListPreference.ListPreferenceSummaryProvider.getInstance());
        writeAccess.setTitle(R.string.write_access);
        writeAccess.setEntries(accessPermissions);
        
        ListPreference deleteAccess = new ListPreference(this, "deleteAccess");
        deleteAccess.setDefaultValue("1");
        deleteAccess.setSummaryProvider(ListPreference.ListPreferenceSummaryProvider.getInstance());
        deleteAccess.setTitle(R.string.delete_access);
        deleteAccess.setEntries(accessPermissions);
        
        PreferenceCategory permission = new PreferenceCategory(this, "permission");
        permission.setTitle(R.string.permission);
        permission.addPreference(writeAccess, deleteAccess);
        
        PreferenceCategory cache = new PreferenceCategory(this, "cache");
        cache.setTitle(R.string.cache);
        
        return new Preference[] {general, files, permission, cache};
    }
}
