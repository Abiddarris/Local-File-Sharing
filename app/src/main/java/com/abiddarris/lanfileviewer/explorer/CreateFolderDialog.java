package com.abiddarris.lanfileviewer.explorer;

import android.os.Bundle;
import android.app.Dialog;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogCreateFolderBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CreateFolderDialog extends DialogFragment {
    
    private DialogCreateFolderBinding binding;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogCreateFolderBinding.inflate(getLayoutInflater());
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setView(binding.getRoot())
            .setTitle(R.string.create_folder)
            .create();
        
        return dialog;
    }
    
    
}
