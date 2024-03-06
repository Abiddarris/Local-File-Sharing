package com.abiddarris.preferences;

import android.app.Dialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ListPreference extends DialogPreference {
    
    private ListEntry[] entries = new ListEntry[0];
    
    public ListPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }
    
    public void setEntries(ListEntry... entries) {
        this.entries = entries;
    }
    
    @Override
    protected Dialog onCreateDialog() {
        String[] choices = new String[entries.length];
        for(int i = 0; i < choices.length; i++) {
            choices[i] = entries[i].getTitle();
        }
        
        return new MaterialAlertDialogBuilder(getFragment().getContext())
                .setTitle(getTitle())
                .setSingleChoiceItems(choices, -1, (dialog, which) -> {})
                .setNegativeButton(android.R.string.cancel, (p1, p2) -> onCancel())
                .setPositiveButton(android.R.string.ok, (p1, p2) -> onSave())
                .create();
    }
    
    
}
