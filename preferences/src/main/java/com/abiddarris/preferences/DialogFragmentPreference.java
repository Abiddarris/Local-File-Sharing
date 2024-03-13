package com.abiddarris.preferences;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogFragmentPreference extends DialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        String key = getArguments()
            .getString(DialogPreference.KEY);
        
        DialogPreference preference = new ViewModelProvider(requireActivity())
            .get(DialogCommunicator.class)
            .find(key);
        
        return preference.onCreateDialog(this);
    }
    
}
