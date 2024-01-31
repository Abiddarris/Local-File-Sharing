package com.abiddarris.lanfileviewer.settings;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.DialogPreference;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogRootEditorBinding;
import com.abiddarris.lanfileviewer.explorer.ExplorerFragment;
import com.abiddarris.lanfileviewer.explorer.FilesSelectorFragment;
import com.abiddarris.lanfileviewer.explorer.LocalFilesSelectorActivity;
import com.abiddarris.lanfileviewer.file.File;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RootEditorDialog extends DialogFragment {
    
    private ActivityResultLauncher<Bundle> getRootsLauncher;
    private AlertDialog dialog;
    private DialogRootEditorBinding binding;
    private RootAdapter adapter;
    
    @Override
    @MainThread
    public void onCreate(Bundle bundle) {
        getRootsLauncher = registerForActivityResult(
            new LocalFilesSelectorActivity.FileContract(getContext()), new ResultCallback());
        
        super.onCreate(bundle);
    }
    
 
    @Override
    @MainThread
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogRootEditorBinding.inflate(getLayoutInflater());
        adapter = new RootAdapter(getContext());
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        
        binding.rootList.setAdapter(adapter);
        binding.rootList.setLayoutManager(layoutManager);
        binding.rootList.addItemDecoration(
            new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
        
        adapter.setRoots(Settings.getRoots(getContext()));
        
        binding.add.setOnClickListener(v -> {
            Bundle bundle1 = new Bundle();
            bundle1.putString(FilesSelectorFragment.ACTION_TEXT, getString(R.string.select));
            
            getRootsLauncher.launch(bundle1);
        });
        binding.resetToDefault.setOnClickListener(v -> {
            adapter.setRoots(Settings.getDefaultRoots(getContext()));
            adapter.notifyDataSetChanged();    
        });
        
        dialog = new MaterialAlertDialogBuilder(getContext())
            .setView(binding.getRoot())
            .setCancelable(false)
            .setNeutralButton(R.string.cancel, (v,v2) -> {})
            .setPositiveButton(R.string.ok, (v,v2) -> save())
            .create();
        
        adapter.setOnRootRemoved((adapter) -> {
            if(adapter.getRoots().size() == 0) {
               dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
            }   
        });
        
        return dialog;
    }
    
    public void save() {
        Settings.setRoots(getContext(), adapter.getRoots());
    }
    
    private class ResultCallback implements ActivityResultCallback<File[]> {
        
        @Override
        public void onActivityResult(File[] files) {
            if(files == null) return;
            
            for(File file : files){
                file.updateDataSync(REQUEST_ABSOLUTE_PATH);
                adapter.addRoot(new java.io.File(file.getAbsolutePath()));
            }
            adapter.notifyDataSetChanged();
            
            if(adapter.getRoots().size() > 0) {
               dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(true);
            }
        }
        
    }
}
