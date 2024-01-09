package com.abiddarris.lanfileviewer.actions;

import android.app.Dialog;
import android.gesture.Prediction;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogOperationOptionsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.concurrent.CountDownLatch;

public class OperationOptionsDialog extends DialogFragment {
   
    private ActionDialog actionDialog;
    private AlertDialog dialog;
    private boolean firstShow = true;
    private CountDownLatch lock;
    private DialogOperationOptionsBinding binding;
    private OperationContext context;
    private OperationOption defaultOptions;
    private OperationOption options;
    
    protected OperationOptionsDialog(ActionDialog actionDialog, OperationContext context) {
        this.actionDialog = actionDialog;
        this.context = context;
        
        binding = DialogOperationOptionsBinding.inflate(actionDialog.getLayoutInflater());
        
        dialog = new MaterialAlertDialogBuilder(actionDialog.getContext())
            .setTitle(R.string.operation_options_title)
            .setView(binding.getRoot())
            .create();
    }
    
    protected void show(FragmentManager manager, CountDownLatch lock, String name, boolean hideReplaceOption) {
    	this.lock = lock;
        
        binding.fileName.setText(String.format(
            actionDialog.getString(R.string.operation_options_description), name
        ));
        binding.rename.setOnClickListener((v) -> setResult(context.renameOption));
        binding.replace.setOnClickListener((v) -> setResult(context.replaceOption));
        binding.skip.setOnClickListener((v) -> setResult(context.skipOption));
        
        if(hideReplaceOption) {
            binding.replace.setVisibility(View.GONE);
        } else {
            binding.replace.setVisibility(View.VISIBLE);
        }
        
        if(firstShow) {
            show(manager, null);
            firstShow = false;
            return;
        }
        
        getDialog().show();
    }
    
    protected void setResult(OperationOption options) {
        this.options = options;
        
        defaultOptions = binding.applyToSimilarItems.isChecked() ? options : null;
        
        actionDialog.getDialog()
            .show();
        getDialog().hide();
        
        lock.countDown();
        lock = null;
    }
    
    public OperationOption getResult() {
    	return options;
    }
    
    public OperationOption getDefaultResult() {
        return defaultOptions;
    }
    
    @Override
    @MainThread
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        return dialog;
    }
    
}