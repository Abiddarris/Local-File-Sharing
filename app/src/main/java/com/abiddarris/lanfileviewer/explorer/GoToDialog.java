package com.abiddarris.lanfileviewer.explorer;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
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
            Context context = getActivity();
                
            file.updateData((e) -> {
                if(file.isDirectory()) {
                    explorer.open(file.getFilePointer());  
                    return;        
                }       
                Toast.makeText(context,
                     String.format(context.getString(R.string.cannot_open_message), file.getPath()), Toast.LENGTH_SHORT)
                    .show();
                FileSource.freeFiles(file);      
            }, REQUEST_IS_DIRECTORY);
              
            dismiss();
        });
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.go_to)
            .setView(binding.getRoot())
            .create();
        
        return dialog;
    }
    
    
}
