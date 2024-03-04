package com.abiddarris.preferences;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public abstract class DialogPreference extends Preference {
    
    static final String KEY = "key";
    
    public DialogPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
        
        new ViewModelProvider(fragment.requireActivity())
            .get(DialogCommunicator.class)
            .add(this);
    }
    
    @Override
    protected void onClick() {
        super.onClick();
        
        Bundle bundle = new Bundle();
        bundle.putString(KEY, getKey());
        
        DialogFragmentPreference dialog = new DialogFragmentPreference();
        dialog.setArguments(bundle);
        dialog.show(getFragment().getChildFragmentManager(), getKey());
    }
    
    protected Dialog onCreateDialog() {
        return new MaterialAlertDialogBuilder(getFragment().getContext())
            .setTitle(getTitle())
            .setNegativeButton(android.R.string.cancel, (p1,p2) -> onCancel())
            .setPositiveButton(android.R.string.ok, (p1,p2) -> onApply())
            .create();
    }
    
    protected void onCancel() {}
    
    protected void onApply() {}
    
}
