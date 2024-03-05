package com.abiddarris.lanfileviewer.settings.material3;

import com.abiddarris.preferences.EditTextPreference;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.preferences.Preference;
import com.abiddarris.preferences.PreferenceCategory;
import com.abiddarris.preferences.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public Preference[] onCreatePreference() {
        EditTextPreference preference = new EditTextPreference(this, "name");
        preference.setTitle(R.string.name);
        preference.setSummaryProvider(EditTextPreference.EditTextSummaryProvider.getInstance());
        
        PreferenceCategory category = new PreferenceCategory(this, "general");
        category.setTitle(R.string.general);
        category.addPreference(preference);
        
        return new Preference[] {category};
    }
}
