package com.abiddarris.lanfileviewer.explorer;

import android.content.Context;
import android.os.Bundle;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogTextInputBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RenameDialog extends DialogFragment {

    private DialogTextInputBinding binding;
    private Explorer explorer;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private File target;
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    
    public RenameDialog(Explorer explorer, File target) {
        this.explorer = explorer;
        this.target = target;
    }

    private FileNameInputValidator validator;

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogTextInputBinding.inflate(getLayoutInflater());

        validator = new FileNameInputValidator(binding, explorer);
        
        binding.textInput.getEditText().addTextChangedListener(validator);
        binding.textInput.getEditText().setText(target.getName());
        binding.cancel.setOnClickListener(v -> dismiss());
        binding.positiveAction.setText(getString(R.string.rename));
        binding.positiveAction.setOnClickListener(v -> {
            String name = binding.textInput
                .getEditText()
                .getText()
                .toString();
            dismiss();
            explorer.setMode(explorer.navigateMode);  
            executor.submit(() -> {
                boolean success = target.rename(name);
                checkSuccess(success);    
            });        
        });
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setView(binding.getRoot())
            .setTitle(target.isFile() ? R.string.rename_file : R.string.rename_folder)
            .create();

        return dialog;
    }
    
    private void checkSuccess(boolean success) {
        handler.post((c) -> {
            explorer.update();
            
            if(!success) {
                Context context = explorer.getContext();         
                int message = target.isFile() ? R.string.fail_rename_file_message : R.string.fail_rename_folder_message;
                Toast.makeText(context, context.getString(message), Toast.LENGTH_SHORT)
                    .show();
            }
        });
    }
}
