package com.abiddarris.lanfileviewer.ui;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gretta.common.annotation.NonNull;

public class AccessRejectedDialog extends DialogFragment {
    
    @Override
    @MainThread
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.access_denied)
            .setPositiveButton(R.string.ok, (p1,p2) -> getActivity().finish())
            .create();
        
        setCancelable(false);
        return dialog;
    }
    
    
}
