package com.abiddarris.lanfileviewer.actions.uploads;

import android.os.Handler;
import android.os.Looper;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import java.util.List;
import java.util.ArrayList;
import com.abiddarris.lanfileviewer.file.Files;
import android.view.View;
import com.abiddarris.lanfileviewer.databinding.DialogActionProgressBinding;

public class UploadRunnable extends ActionRunnable {

    private DialogActionProgressBinding view;
    private Handler handler = new Handler(Looper.getMainLooper());
    private File dest;
    private File[] items;
    private FileSource destSource;
    
    public UploadRunnable(FileSource destSource, File dest, File[] items) {
        this.destSource = destSource;
        this.items = items;
        this.dest = dest;
    }
    
    @Override
    public String getTitle() {
        ActionDialog dialog = getDialog();
        return String.format(
            dialog.getString(R.string.upload_dialog_title), items.length);
    }

    @Override
    public void onExecute() throws Exception {
        view = getView();
        
        prepare();

        List<File> files = new ArrayList<>();
        for (File file : items) {
            Files.getFilesTree(files, file);
        }

        startUpload();

        File parent = items[0].getParentFile();
        for (int i = 0; i < files.size(); ++i) {
            File originalFile = files.get(i);
            String localPath = originalFile.getPath()
                .replace(parent.getPath(), "");
            
            File destFile = getDialog()
                .getFile(destSource, dest.getPath() + localPath);
           
            if(destFile == null) continue;
            
            if (originalFile.isDirectory()) {
                uploadDirectory(destFile, i, files.size());
            }
        }
    }

    private void uploadDirectory(File file, int index, int size) {
        index++;

        updateUI(file.getName(), index, size, 0, 1);

        boolean success = file.makeDirs();
        
        updateUI(file.getName(), index, size, 1, 1);
    }

    private void updateUI(String name, int index, int totalFiles, double progress, double totalSize) {
        handler.post(() -> {
            view.name.setText(name);
            view.progress.setText((index) + "/" + totalFiles);
            view.progressIndicator.setMax((int) totalSize);
            view.progressIndicator.setProgress((int) progress);
            view.progressPercent.setText(Math.round(progress / totalSize * 100) + "%");
        });
    }

    private void startUpload() {
        handler.post(() -> {
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
