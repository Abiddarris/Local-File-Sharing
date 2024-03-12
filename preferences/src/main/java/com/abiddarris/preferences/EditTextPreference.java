package com.abiddarris.preferences;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.abiddarris.preferences.databinding.LayoutEditTextBinding;

public class EditTextPreference extends DialogPreference {

    private String defaultValue;
    private LayoutEditTextBinding binding;

    public EditTextPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }

    @Override
    protected View onCreateView(LayoutInflater inflater) {
        String value = getValueOrDefault();
        binding = LayoutEditTextBinding.inflate(inflater);

        binding.textInput.getEditText().setText(value);

        return binding.getRoot();
    }

    @Override
    protected void onSave() {
        super.onSave();

        storeString(binding.textInput.getEditText().getText().toString());
        refillView();
    }
    
    public String getValueOrDefault() {
    	String value = getNonNullDataStore().getString(getKey());
        return value != null ? value : getDefaultValue();
    }

    public static class EditTextSummaryProvider implements SummaryProvider {

        private static final EditTextSummaryProvider summaryProvider =
                new EditTextSummaryProvider();

        @Override
        public String getSummary(Preference preference) {
            EditTextPreference editTextPreference = (EditTextPreference)preference;
            String value = editTextPreference.getValueOrDefault();
            return value == null ? "" : value;
        }

        public static EditTextSummaryProvider getInstance() {
            return summaryProvider;
        }
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
