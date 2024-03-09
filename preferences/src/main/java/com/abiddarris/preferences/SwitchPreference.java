package com.abiddarris.preferences;

import android.view.View;

import com.abiddarris.preferences.databinding.LayoutSwitchPreferenceBinding;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SwitchPreference extends Preference {
    
    public SwitchPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }
    
    @Override
    protected View createView() {
        return LayoutSwitchPreferenceBinding.inflate(getFragment().getLayoutInflater())
            .getRoot();
    }
    
    @Override
    protected void fillView(View view) {
        super.fillView(view);
        
        MaterialSwitch materialSwitch = view.findViewById(R.id.materialSwitch);
        materialSwitch.setOnClickListener(v -> onSave(materialSwitch));
        materialSwitch.setChecked(getNonNullDataStore()
            .getBoolean(getKey()));
    }
    
    @Override
    protected void onClick(View view) {
        MaterialSwitch materialSwitch = view.findViewById(R.id.materialSwitch);
        materialSwitch.setChecked(!materialSwitch.isChecked());
        
        onSave(materialSwitch);
    }
    
    protected void onSave(MaterialSwitch materialSwitch) {
        storeBoolean(materialSwitch.isChecked());
    }
    
}
