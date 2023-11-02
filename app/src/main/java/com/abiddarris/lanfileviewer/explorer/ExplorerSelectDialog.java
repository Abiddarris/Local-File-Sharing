package com.abiddarris.lanfileviewer.explorer;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ExplorerSelectDialog extends DialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setCancelable(false)
            .setView(R.layout.dialog_select_explorer)
            .create();
        
        return dialog;
    }
    
    @Override
    @MainThread
    public void onStart() {
        super.onStart();
        
        
        
        
    }
    
}
