package com.abiddarris.preferences;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.abiddarris.preferences.databinding.LayoutEditTextBinding;

public class EditTextPreference extends DialogPreference {

    private LayoutEditTextBinding binding;

    public EditTextPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }

    @Override
    protected View onCreateView(LayoutInflater inflater) {
        String value = getNonNullDataStore().getString(getKey());
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

    public static class EditTextSummaryProvider implements SummaryProvider {

        private static final EditTextSummaryProvider summaryProvider = new EditTextSummaryProvider();
        
        @Override
        public String getSummary(Preference preference) {
            String value = preference.getNonNullDataStore()
                .getString(preference.getKey());
            return value == null ? "" : value;
        }
        
        public static EditTextSummaryProvider getInstance() {
            return summaryProvider;
        }
    }
}
