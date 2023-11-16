package com.abiddarris.lanfileviewer.ui.actions;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.UploadDialogBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadDialog extends DialogFragment {
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private File[] items;
    private Handler handler = new Handler(Looper.getMainLooper());
    private UploadDialogBinding view;
    
    public UploadDialog(File[] items) {
        this.items = items;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        setCancelable(false);
        
        view = UploadDialogBinding.inflate(getLayoutInflater());
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle("p")
            .setView(view.getRoot())
            .create();
        
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        executor.submit(new UploadRunnable());
    }
    
    public class UploadRunnable extends BaseRunnable {
        
        @Override
        public void onExecute() throws Exception {
            prepare();
            
            List<File> files = new ArrayList<>();
            for(File file : items) {
                Files.getFilesTree(files, file);
            }
        }
        
        public void prepare() {
        	handler.post(() -> {
                view.name.setVisibility(View.INVISIBLE);
                view.progressPercent.setVisibility(View.INVISIBLE);
                    
                view.progress.setText(R.string.preparing);    
            });
        }
    }
    
}
