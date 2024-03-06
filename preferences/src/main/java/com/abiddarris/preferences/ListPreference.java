package com.abiddarris.preferences;

import android.app.Dialog;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ListPreference extends DialogPreference {

    private ListEntry[] entries = new ListEntry[0];
    private String defaultValue;

    public ListPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }

    public void setEntries(ListEntry... entries) {
        this.entries = entries;
    }

    @Override
    protected Dialog onCreateDialog() {
        String defaultValue = getDefaultValue();
        String[] choices = new String[entries.length];
        int selection = -1;
        for (int i = 0; i < choices.length; i++) {
            choices[i] = entries[i].getTitle();
            
            if(selection == -1 && entries[i].getValue().equalsIgnoreCase(defaultValue)) {
                selection = i;
            }
        }

        return new MaterialAlertDialogBuilder(getFragment().getContext())
                .setTitle(getTitle())
                .setSingleChoiceItems(choices, selection, (dialog, which) -> {})
                .setNegativeButton(android.R.string.cancel, (p1, p2) -> onCancel())
                .setPositiveButton(android.R.string.ok, (p1, p2) -> onSave())
                .create();
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
