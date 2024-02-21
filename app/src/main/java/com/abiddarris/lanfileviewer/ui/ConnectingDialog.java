package com.abiddarris.lanfileviewer.ui;

import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ConnectingDialog extends DialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.connecting_dialog_title)
            .setMessage(R.string.connecting_dialog_desc)
            .create();
        
        return dialog;
    }
    
    
}
