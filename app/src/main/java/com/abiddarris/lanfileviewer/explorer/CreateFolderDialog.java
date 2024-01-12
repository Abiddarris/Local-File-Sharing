package com.abiddarris.lanfileviewer.explorer;

import android.content.Context;
import android.os.Bundle;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogTextInputBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateFolderDialog extends DialogFragment {

    private Context context;
    private DialogTextInputBinding binding;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Explorer explorer;
    private FileNameInputValidator validator;
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    private String failCreateFoldersMessage;
    
    public static final String TAG = Log.getTag(CreateFolderDialog.class);

    public CreateFolderDialog(Explorer explorer) {
        this.explorer = explorer;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogTextInputBinding.inflate(getLayoutInflater());
        
        validator = new FileNameInputValidator(binding, explorer);
        
        binding.textInput.getEditText().addTextChangedListener(validator);
        binding.positiveAction.setText(getString(R.string.create));
        binding.positiveAction.setOnClickListener(v -> {
            File folder = validator.createFileFromInput();
            dismiss(); 
            executor.execute(() -> {
                boolean sucess = folder.makeDirs();
                refreshExplorer();
                if(!sucess) {
                    showFailedToast();
                } 
            });    
        });
        binding.cancel.setOnClickListener(v -> dismiss());
        
        context = getActivity().getApplication();
        failCreateFoldersMessage = getString(R.string.fail_creating_folders);
        
        AlertDialog dialog =
                new MaterialAlertDialogBuilder(getContext())
                        .setView(binding.getRoot())
                        .setTitle(R.string.create_folder)
                        .create();

        return dialog;
    }

    private void refreshExplorer() {
        handler.post((c) -> explorer.refresh());
    }

    private void showFailedToast() {
        handler.post((c) -> 
            Toast.makeText(context, failCreateFoldersMessage , Toast.LENGTH_SHORT).show());
    }
}
