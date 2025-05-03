package com.abiddarris.lanfileviewer.settings;

import static com.abiddarris.common.android.preferences.DialogPreference.PREFERENCE;
import static com.abiddarris.lanfileviewer.file.Requests.REQUEST_ABSOLUTE_PATH;

import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abiddarris.common.android.dialogs.BaseDialogFragment;
import com.abiddarris.common.android.preferences.DialogPreference;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogRootEditorBinding;
import com.abiddarris.lanfileviewer.explorer.FilesSelectorFragment;
import com.abiddarris.lanfileviewer.explorer.LocalFilesSelectorActivity;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RootEditorDialog extends BaseDialogFragment<Void> {
    
    private ActivityResultLauncher<Bundle> getRootsLauncher;
    private AlertDialog dialog;
    private DialogRootEditorBinding binding;
    private RootAdapter adapter;

    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        super.onCreateDialog(builder, savedInstanceState);

        DialogPreference preference = getDialogPreference();
        if (preference == null) {
            dismiss();
            return;
        }

        getRootsLauncher = registerForActivityResult(
                new LocalFilesSelectorActivity.FileContract(getContext()), new ResultCallback());

        binding = DialogRootEditorBinding.inflate(getLayoutInflater());

        builder.setView(binding.getRoot())
                .setTitle(preference.getTitle())
                .setPositiveButton(android.R.string.ok, (d, w) ->
                        Settings.setRoots(getContext(), adapter.getRoots()))
                .setNegativeButton(android.R.string.cancel, (d, w) -> {});
    }

    @Nullable
    private DialogPreference getDialogPreference() {
        return getVariable(PREFERENCE);
    }

    @Override
    protected void onDialogCreated(AlertDialog dialog, Bundle savedInstanceState) {
        super.onDialogCreated(dialog, savedInstanceState);

        this.dialog = dialog;

        adapter = new RootAdapter(getContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        binding.rootList.setAdapter(adapter);
        binding.rootList.setLayoutManager(layoutManager);
        binding.rootList.addItemDecoration(
                new DividerItemDecoration(requireContext(), layoutManager.getOrientation()));

        adapter.setRoots(Settings.getRoots(getContext()));

        binding.add.setOnClickListener(v -> {
            Bundle bundle1 = new Bundle();
            bundle1.putString(FilesSelectorFragment.ACTION_TEXT,
                    requireContext().getString(R.string.select));

            getRootsLauncher.launch(bundle1);
        });

        binding.resetToDefault.setOnClickListener(v -> {
            adapter.setRoots(Settings.getDefaultRoots(getContext()));
            adapter.notifyDataSetChanged();
        });

        adapter.setOnRootRemoved((adapter) -> {
            if(adapter.getRoots().isEmpty()) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(false);
            }
        });
    }
    
    private class ResultCallback implements ActivityResultCallback<FilePointer[]> {
        
        @Override
        public void onActivityResult(FilePointer[] files) {
            if(files == null) return;
            
            for(FilePointer pointer : files){
                File file = pointer.get();
                
                file.updateDataSync(REQUEST_ABSOLUTE_PATH);
                adapter.addRoot(new java.io.File(file.getAbsolutePath()));
                
                FileSource.freeFiles(file);
            }
            adapter.notifyDataSetChanged();
            
            if(!adapter.getRoots().isEmpty()) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        }
        
    }
}
