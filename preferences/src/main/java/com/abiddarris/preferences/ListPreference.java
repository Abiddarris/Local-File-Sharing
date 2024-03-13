package com.abiddarris.preferences;

import android.app.Dialog;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ListPreference extends DialogPreference {

    private int selection;
    private ListEntry[] entries = new ListEntry[0];
    private String defaultValue;

    public ListPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }

    public void setEntries(ListEntry... entries) {
        this.entries = entries;
    }
    
    public ListEntry[] getEntries() {
        return entries;
    }

    @Override
    protected Dialog onCreateDialog(DialogFragment fragment) {
        String value = getValueOrDefault();
        
        String[] choices = new String[entries.length];
        int selection = -1;
        for (int i = 0; i < choices.length; i++) {
            choices[i] = entries[i].getTitle();
            
            if(selection == -1 && entries[i].getValue().equals(value)) {
                selection = i;
            }
        }

        return new MaterialAlertDialogBuilder(getFragment().getContext())
                .setTitle(getTitle())
                .setSingleChoiceItems(choices, selection, (dialog, which) -> this.selection = which)
                .setNegativeButton(android.R.string.cancel, (p1, p2) -> onCancel())
                .setPositiveButton(android.R.string.ok, (p1, p2) -> {
                    fragment.dismiss();
                    onSave();
                })
                .create();
    }
    
    @Override
    protected void onSave() {
        super.onSave();
        
        storeString(getEntries()[selection].getValue());
        refillView();
    }
    
    public String getValue() {
        return getNonNullDataStore().getString(getKey());
    }
    
    public String getValueOrDefault() {
        String value = getValue();
        
        return value != null ? value : getDefaultValue();
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public static class ListPreferenceSummaryProvider implements SummaryProvider {
       
        private static final ListPreferenceSummaryProvider provider = new ListPreferenceSummaryProvider();
        
        @Override
        public String getSummary(Preference preference) {
            ListPreference listPreferences = ((ListPreference)preference);
            String value = listPreferences.getValueOrDefault();
            
            for(ListEntry entry : listPreferences.getEntries()) {
            	if(entry.getValue().equals(value)) {
                    return entry.getTitle();
                }
            }
            return "";
        }
        
        public static ListPreferenceSummaryProvider getInstance() {
        	return provider;
        }
        
    }
}
