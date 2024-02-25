package com.abiddarris.lanfileviewer.ui;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.FileExplorerActivity;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogTextInputBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FillPasswordDialog extends DialogFragment {
    
    private DialogTextInputBinding binding;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogTextInputBinding.inflate(getLayoutInflater());
        binding.positiveAction.setText(R.string.login);
        binding.positiveAction.setEnabled(true);
        binding.positiveAction.setOnClickListener(v -> {
            ((FileExplorerActivity)getActivity())
                .connectAsync(binding.textInput.getEditText()
                    .getText().toString());
            dismiss();    
        });
        binding.cancel.setOnClickListener(v -> {
            getActivity().finish();
            dismiss();    
        });
        binding.textInput.getEditText()
            .setSingleLine(true);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setView(binding.getRoot())
            .setTitle(R.string.enter_password)
            .create();
        setCancelable(false);
        
        return dialog;
    }
    
    
}
