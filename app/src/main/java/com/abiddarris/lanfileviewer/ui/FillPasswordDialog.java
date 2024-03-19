package com.abiddarris.lanfileviewer.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogTextInputBinding;
import com.abiddarris.lanfileviewer.ui.ScanFragment.ConnectViewModel;
import com.abiddarris.lanfileviewer.utils.NonBlankTextValidator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FillPasswordDialog extends DialogFragment {
    
    private DialogTextInputBinding binding;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogTextInputBinding.inflate(getLayoutInflater());
        binding.positiveAction.setText(R.string.login);
        binding.positiveAction.setOnClickListener(v -> {
            new ViewModelProvider(getActivity())    
                .get(ConnectViewModel.class)
                .connectAsync(binding.textInput.getEditText()
                    .getText().toString());
                
            dismiss();    
        });
        binding.cancel.setOnClickListener(v -> {
            dismiss();    
        });
        binding.textInput.getEditText()
            .setSingleLine(true);
        binding.textInput.getEditText()
            .addTextChangedListener(
                new NonBlankTextValidator(binding.positiveAction));
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setView(binding.getRoot())
            .setTitle(R.string.enter_password)
            .create();
        setCancelable(false);
        
        return dialog;
    }
    
    
}
