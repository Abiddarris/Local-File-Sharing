package com.abiddarris.lanfileviewer.actions;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogActionProgressBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.gretta.util.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActionDialog extends DialogFragment {

    private ActionRunnable runnable;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private DialogActionProgressBinding view;
    private Handler handler = new Handler(Looper.getMainLooper());
    private OperationOptionsDialog optionsDialog;
    
    public ActionDialog(ActionRunnable runnable) {
        this.runnable = runnable;
        
        runnable.attachDialog(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        setCancelable(false);

        view = DialogActionProgressBinding.inflate(getLayoutInflater());
        view.cancel.setOnClickListener((v) -> {
            dismiss();
        });
        
        optionsDialog = new OperationOptionsDialog(this);

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(runnable.getTitle())
                        .setView(view.getRoot())
                        .create();

        executor.submit(runnable);
        
        return dialog;
    }
    
    public File getFile(FileSource source, String path) throws Exception {
        File file = source.getFile(path);
        file.updateDataSync();
        
        if(!file.exists()) return file;
        
        OperationOptions options = optionsDialog.getDefaultResult();
        if(options != null) return options.transform(file);
        
        CountDownLatch lock = new CountDownLatch(1);
        
        handler.post(() -> {
            getDialog().hide();
                
            optionsDialog.show(getParentFragmentManager(), lock, file.getName());
        });
        
        try {
        	lock.await();
            options = optionsDialog.getResult();
            
            return options.transform(file);
        } catch(InterruptedException err) {
        	err.printStackTrace();
        }
        return null;
    }

    public DialogActionProgressBinding getViewBinding() {
        return this.view;
    }

}
