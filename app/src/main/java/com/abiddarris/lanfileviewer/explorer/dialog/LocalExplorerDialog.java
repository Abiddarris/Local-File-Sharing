package com.abiddarris.lanfileviewer.explorer.dialog;

import android.app.Dialog;
import android.app.FragmentContainer;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentContainerView;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.explorer.LocalExplorerFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LocalExplorerDialog extends DialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        return new MaterialAlertDialogBuilder(getContext())
            .setView(R.layout.upload_dialog)
            .create();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        getChildFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragmentContainerView, LocalExplorerFragment.class, null)
            .commit();
        
    }
    
    
}
