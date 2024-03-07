package com.abiddarris.preferences;
import android.view.View;
import com.abiddarris.preferences.databinding.LayoutSwitchPreferenceBinding;

public class SwitchPreference extends Preference {
    
    public SwitchPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }
    
    @Override
    protected View createView() {
        return LayoutSwitchPreferenceBinding.inflate(getFragment().getLayoutInflater())
            .getRoot();
    }
    
}
