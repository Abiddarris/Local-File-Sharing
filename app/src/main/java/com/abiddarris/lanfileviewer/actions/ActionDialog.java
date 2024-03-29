package com.abiddarris.lanfileviewer.actions;

import static com.abiddarris.lanfileviewer.file.Requests.*;

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
import com.abiddarris.lanfileviewer.explorer.Explorer;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
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
    private Explorer explorer;
    private DialogActionProgressBinding view;
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    private OperationContext context = new OperationContext();
    private OperationOptionsDialog optionsDialog;
    
    public static final String TAG = Log.getTag(ActionDialog.class);
    
    public ActionDialog(Explorer explorer, ActionRunnable runnable) {
        this.runnable = runnable;
        this.explorer = explorer;
        
        runnable.attachDialog(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        setCancelable(false);

        view = DialogActionProgressBinding.inflate(getLayoutInflater());
        view.cancel.setOnClickListener((v) -> {
            dismiss();
        });
        
        optionsDialog = new OperationOptionsDialog(this, context);

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(runnable.getTitle())
                        .setView(view.getRoot())
                        .create();

        executor.submit(runnable);
        
        return dialog;
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        
        executor.shutdownNow();
        if(explorer != null)
            explorer.refresh();
        
        if(optionsDialog.isAdded()) {
            optionsDialog.dismiss();
        }
    }
    
    public File getFile(FileSource source, File src, String path) throws Exception {
        File file = source.getFile(path);
        file.updateDataSync(REQUEST_EXISTS, REQUEST_IS_DIRECTORY);
        
        if(!file.exists()) return file;
        
        try {
        	File globalTransform = context.runGlobalTransform(file);
            if(globalTransform != file) {
                FileSource.freeFiles(file);
            }
            return globalTransform;
        } catch(OperationException err) {
            Log.debug.log(TAG, "showing dialog");
        }
        
        OperationOption options = optionsDialog.getDefaultResult();
        if(options != null) {
            File defaultTransform = options.transform(file);
            if(defaultTransform != file) {
                FileSource.freeFiles(file);
            }
            return defaultTransform;
        } 
        CountDownLatch lock = new CountDownLatch(1);
        
        handler.post((c) -> {
            getDialog().hide();
                
            optionsDialog.show(getParentFragmentManager(), lock, file.getName(), src.getPath().equalsIgnoreCase(path));
        });
        
        File transform = null;
        try {
        	lock.await();
            options = optionsDialog.getResult();
            
            transform = options.transform(file);
        } catch(InterruptedException err) {
        	err.printStackTrace();
        } finally {
            if(transform != file) {
                FileSource.freeFiles(file);
            }
        }
        return transform;
    }

    public DialogActionProgressBinding getViewBinding() {
        return this.view;
    }

}
