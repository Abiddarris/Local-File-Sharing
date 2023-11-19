package com.abiddarris.lanfileviewer.ui.actions;

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
import com.abiddarris.lanfileviewer.databinding.UploadDialogBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadDialog extends DialogFragment {
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private File dest;
    private File[] items;
    private FileSource networkFileSource;
    private Handler handler = new Handler(Looper.getMainLooper());
    private UploadDialogBinding view;
    
    public UploadDialog(FileSource networkFileSource, File dest, File[] items) {
        this.networkFileSource = networkFileSource;
        this.items = items;
        this.dest = dest;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        setCancelable(false);
        
        String title = String.format(getString(R.string.upload_dialog_title), items.length);
        
        view = UploadDialogBinding.inflate(getLayoutInflater());
        view.cancel.setOnClickListener((v) -> {
            dismiss();
        });
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(title)
            .setView(view.getRoot())
            .create();
        
        executor.submit(new UploadRunnable());
        
        return dialog;
    }
 
    public class UploadRunnable extends BaseRunnable {
        
        @Override
        public void onExecute() throws Exception {
            prepare();
            
            List<File> files = new ArrayList<>();
            for(File file : items) {
                Files.getFilesTree(files, file);
            }
            
            Log.debug.log("h",files);
            startUpload();
            
            File parent = items[0].getParentFile();
            for(int i = 0; i < files.size(); ++i) {
            	File originalFile = files.get(i);
                String filePath = originalFile.getPath()
                    .replace(parent.getPath(), "");
                File networkFile = networkFileSource.getFile(dest.getPath() + filePath);
                if(originalFile.isDirectory()) {
                    uploadDirectory(networkFile, i, files.size());
                }
            }
        }

        private void uploadDirectory(File file, int index, int size) {
            index++;
            
            updateUI(file.getName(),index,size,0,1);
            
            boolean success = file.makeDirs();
            Log.debug.log("h", "Success");
            
            updateUI(file.getName(),index,size,1,1);
        }
        
        private void updateUI(String name, int index, int totalFiles, double progress, double totalSize) {
            handler.post(() -> {
                view.name.setText(name);
                view.progress.setText((index) + "/" + totalFiles);
                view.progressIndicator.setMax((int)totalSize);
                view.progressIndicator.setProgress((int)progress);
                view.progressPercent.setText(
                    Math.round(progress/totalSize * 100) + "%"
                );    
            });
        }

        private void startUpload() {
            handler.post(() -> {
                Log.debug.log("h", "test") ; 
                view.name.setVisibility(View.VISIBLE);
                view.progressPercent.setVisibility(View.VISIBLE);
            });
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
