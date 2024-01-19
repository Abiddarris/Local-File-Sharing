package com.abiddarris.lanfileviewer.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RootEditorDialog extends DialogFragment {
    
    private DialogRootEditorBinding binding;
    private RootAdapter adapter;
 
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
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setView(binding.getRoot())
            .setCancelable(false)
            .setNeutralButton(R.string.cancel, (v,v2) -> {})
            .setPositiveButton(R.string.ok, (v,v2) -> save())
            .create();
        
        return dialog;
    }
    
    public void save() {
    }
}
