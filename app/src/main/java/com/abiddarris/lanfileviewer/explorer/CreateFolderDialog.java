package com.abiddarris.lanfileviewer.explorer;

import android.os.Bundle;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogCreateFolderBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateFolderDialog extends DialogFragment {

    private DialogCreateFolderBinding binding;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Explorer explorer;
    private FileSource source;
    private String parentPath;
    
    public static final String TAG = Log.getTag(CreateFolderDialog.class);

    public CreateFolderDialog(Explorer explorer) {
        this.explorer = explorer;
        
        File parent = explorer.getParent();
       
        parentPath = parent.getPath();
        source = parent.getSource();
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogCreateFolderBinding.inflate(getLayoutInflater());
        binding.name.getEditText().addTextChangedListener(new TextListener());
        binding.create.setOnClickListener(v -> {
            String name = binding.name.getEditText().getText().toString();
            File folder = source.getFile(parentPath + "/" + name);
            executor.execute(() -> {
                boolean sucess = folder.makeDirs();
                if(!sucess) {
                    showFailedToast();
                }
            });    
        });
        binding.cancel.setOnClickListener(v -> dismiss());
        
        AlertDialog dialog =
                new MaterialAlertDialogBuilder(getContext())
                        .setView(binding.getRoot())
                        .setTitle(R.string.create_folder)
                        .create();

        return dialog;
    }

    private void showFailedToast() {
        getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), getString(R.string.fail_creating_folders) , Toast.LENGTH_SHORT).show();
            dismiss(); 
            explorer.update();       
        });
    }

    private class TextListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

        @Override
        public void afterTextChanged(Editable editable) {
            String name = editable.toString();
            binding.create.setEnabled(false);
            if(name.isBlank()) {
                binding.name.setErrorEnabled(false);
                return;
            }
            
            File folder = source.getFile(parentPath + "/" + name);
            executor.execute(() -> {
                try {
                    validateInput(folder);
                } catch (Exception e) {
                    Log.err.log(TAG, e);
                }
            });
        }

        private void validateInput(final File folder) throws Exception {
            folder.updateDataSync();
                    
            if(!folder.exists()) {
                getActivity().runOnUiThread(() -> {
                    binding.create.setEnabled(true);
                    binding.name.setErrorEnabled(false);
                });
            } else {
                getActivity().runOnUiThread(() -> {
                    binding.name.setErrorEnabled(true);
                    binding.name.setError(getString(R.string.file_already_exists));
                });
            }
        }
    }
}
