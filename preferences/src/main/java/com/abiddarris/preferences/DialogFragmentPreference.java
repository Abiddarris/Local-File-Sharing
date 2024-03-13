package com.abiddarris.preferences;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogFragmentPreference extends DialogFragment {
    
    private DialogPreference preference;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        String key = getArguments()
            .getString(DialogPreference.KEY);
        
        preference = new ViewModelProvider(requireActivity())
            .get(DialogCommunicator.class)
            .find(key);
        
        return preference.onCreateDialog(this);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        preference.onDialogDestroy();
        
        super.onDestroy();
    }
    
    
}
