package com.abiddarris.lanfileviewer.explorer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogSortBinding;
import com.abiddarris.lanfileviewer.sorter.FileSorter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SortByDialog extends DialogFragment {
    
    private DialogSortBinding binding;
    
    public static final String SORT_TYPE = "sortType";
    
    @Override
    @MainThread
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getContext());
        int sortType = preference.getInt(SORT_TYPE, FileSorter.ASCENDING | FileSorter.NAME);
        
        binding = DialogSortBinding.inflate(getLayoutInflater());
        binding.ascending.setChecked(true);
        binding.decending.setChecked((sortType & FileSorter.DECENDING) != 0);
        
        binding.name.setChecked((sortType & FileSorter.NAME) != 0);
        binding.date.setChecked((sortType & FileSorter.DATE) != 0);
        binding.type.setChecked((sortType & FileSorter.TYPE) != 0);
        binding.size.setChecked((sortType & FileSorter.SIZE) != 0);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.sort_by)
            .setView(binding.getRoot())
            .setNegativeButton(R.string.cancel, (d, id) -> onCancel())
            .setPositiveButton(R.string.ok, (d,id) -> onOk())
            .create();
        
        return dialog;
    }

    private void onOk() {
        int sort = binding.ascending.isChecked() ? FileSorter.ASCENDING : FileSorter.DECENDING;
        int sortBy = binding.name.isChecked() ? FileSorter.NAME :
             (binding.date.isChecked() ? FileSorter.DATE : 
             (binding.type.isChecked() ? FileSorter.TYPE : FileSorter.SIZE));
        
        PreferenceManager.getDefaultSharedPreferences(getContext())
            .edit().putInt(SORT_TYPE, sort | sortBy)
            .commit();
    }
    
    private void onCancel() {
    }

}
