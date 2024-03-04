package com.abiddarris.preferences;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.abiddarris.preferences.databinding.LayoutEditTextBinding;

public class EditTextPreference extends DialogPreference {
    
    public EditTextPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }
    
    @Override
    protected View createDialogView(LayoutInflater inflater) {
        String value = getNonNullDataStore()
                .getString(getKey());
        
        LayoutEditTextBinding binding = LayoutEditTextBinding.inflate(inflater);
        binding.textInput.getEditText()
            .setText(value);
        
        return binding.getRoot();
    }
    
    
}
