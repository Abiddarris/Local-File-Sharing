package com.abiddarris.lanfileviewer.ui;

import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ConnectingDialog extends DialogFragment {
    
    public static final String NAME = "name";
  
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        String message = getString(R.string.connecting_dialog_desc);
        Bundle argument = getArguments();
        String name = argument.getString(NAME);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.connecting_dialog_title)
            .setMessage(String.format(message, name))
            .create();
        
        return dialog;
    }
    
    
}
