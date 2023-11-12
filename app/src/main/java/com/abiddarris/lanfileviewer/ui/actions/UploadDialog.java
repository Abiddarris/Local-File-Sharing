package com.abiddarris.lanfileviewer.ui.actions;

import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.UploadDialogBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;

public class UploadDialog extends DialogFragment {
    
    private File[] items;
    private UploadDialogBinding view;
    
    public UploadDialog(File[] items) {
        this.items = items;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        setCancelable(false);
        
        view = UploadDialogBinding.inflate(getLayoutInflater());
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle("p")
            .setView(view.getRoot())
            .create();
        
        return dialog;
    }
    
    @Override
    public void onStart() {
        
        super.onStart();
    }
    
    public class UploadRunnable extends BaseRunnable {
        
        @Override
        public void onExecute() throws Exception {
            
        }
        
    }
    
}
