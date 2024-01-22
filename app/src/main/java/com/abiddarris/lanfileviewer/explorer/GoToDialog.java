package com.abiddarris.lanfileviewer.explorer;
import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogTextInputBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class GoToDialog extends DialogFragment {
    
    private Explorer explorer;
    private FileSource source;
    
    public GoToDialog(Explorer explorer, FileSource source) {
        this.explorer = explorer;
        this.source = source;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        DialogTextInputBinding binding = DialogTextInputBinding.inflate(getLayoutInflater());
        binding.cancel.setOnClickListener(v -> dismiss());
        binding.positiveAction.setEnabled(true);
        binding.positiveAction.setText(R.string.go);
        binding.positiveAction.setOnClickListener(v -> {
            File file = source.getFile(binding.textInput.getEditText()
                .getText()
                .toString());
            file.updateData((e) -> {
                explorer.open(file);  
            });
              
            dismiss();
        });
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.go_to)
            .setView(binding.getRoot())
            .create();
        
        return dialog;
    }
    
    
}
