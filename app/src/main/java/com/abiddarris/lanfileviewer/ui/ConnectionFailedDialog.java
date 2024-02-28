package com.abiddarris.lanfileviewer.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gretta.common.annotation.NonNull;

public class ConnectionFailedDialog extends DialogFragment {
    
    public static final String MESSAGE = "message";
    
    @Override
    @MainThread
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        Bundle arguments = getArguments();
        String message = arguments.getString(MESSAGE);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.connection_failed)
            .setMessage(message)
            .setPositiveButton(R.string.ok, (p1,p2) -> getActivity().finish())
            .create();
        
        setCancelable(false);
        return dialog;
    }
    
    
}
