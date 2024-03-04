package com.abiddarris.preferences;

import android.view.LayoutInflater;
import android.view.View;

public class EditTextPreference extends DialogPreference {
    
    public EditTextPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }
    
    @Override
    protected View createDialogView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.layout_edit_text, null);
    }
    
    
}
