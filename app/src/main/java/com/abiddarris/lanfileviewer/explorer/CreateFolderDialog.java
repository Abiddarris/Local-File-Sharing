package com.abiddarris.lanfileviewer.explorer;

import android.os.Bundle;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
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

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(getContext())
                        .setView(binding.getRoot())
                        .setTitle(R.string.create_folder)
                        .create();

        return dialog;
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
            
            File folder = source.getFile(parentPath + "/" + name);
            executor.execute(() -> {
                try {
                    folder.updateDataSync();
                    if(!folder.exists()) {
                        getActivity().runOnUiThread(() -> binding.create.setEnabled(true));
                    }  
                } catch (Exception e) {
                    Log.err.log(TAG, e);
                }
            });
        }
    }
}
