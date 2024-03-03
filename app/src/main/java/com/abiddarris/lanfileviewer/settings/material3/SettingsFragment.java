package com.abiddarris.lanfileviewer.settings.material3;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.preferences.Preference;
import com.abiddarris.preferences.PreferenceCategory;
import com.abiddarris.preferences.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public Preference[] onCreatePreference() {
        Preference preference = new Preference(getContext(), "name");
        preference.setTitle(R.string.name);
        preference.setSummary("test");
        
        PreferenceCategory category = new PreferenceCategory(getContext(), "general");
        category.setTitle(R.string.general);
        category.addPreference(preference);
        
        return new Preference[] {category};
    }
}
