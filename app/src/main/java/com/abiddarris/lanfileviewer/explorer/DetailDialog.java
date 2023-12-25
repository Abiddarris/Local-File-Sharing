package com.abiddarris.lanfileviewer.explorer;
import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogDetailBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DetailDialog extends DialogFragment {
    
    private DialogDetailBinding binding;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogDetailBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setView(binding.getRoot())
            .setPositiveButton(R.string.ok, (d, type) -> {})
            .create();
        
        return dialog;
    }
    
    
}
