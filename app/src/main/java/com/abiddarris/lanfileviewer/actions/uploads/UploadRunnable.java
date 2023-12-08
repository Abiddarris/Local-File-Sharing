package com.abiddarris.lanfileviewer.actions.uploads;

import android.os.Handler;
import android.os.Looper;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.sharing.NetworkOutputStream;
import com.gretta.util.log.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
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
    
    public static final String TAG = Log.getTag(UploadRunnable.class);
    
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
            if(Thread.currentThread().isInterrupted()) {
                Log.debug.log(TAG, "Canceling upload...");
                return;
            }
            
            File originalFile = files.get(i);
            
            String localPath = originalFile.getPath()
                .replace(parent.getPath(), "");
            
            File destFile = getDialog()
                .getFile(destSource, dest.getPath() + localPath);
           
            if(destFile == null) continue;
            
            updateFileInfo(originalFile.getName(), i + 1, files.size());
            
            if (originalFile.isDirectory()) {
                uploadDirectory(destFile);
            } else {
                uploadFile(originalFile, destFile);
            }
        }
    }

    private void uploadFile(File src, File dest) throws IOException {
        setMaxProgress(src.length());
        updateProgress(0);
        
        NetworkOutputStream networkStream = (NetworkOutputStream) dest.newOutputStream();
        networkStream.setMimeType(src.getMimeType());
        networkStream.open(src.length());
        
        BufferedOutputStream os = new BufferedOutputStream(networkStream);
        
        BufferedInputStream is = new BufferedInputStream(src.newInputStream());
        byte[] buffer = new byte[4 * 1024];
        int len;
        long writtenBytes = 0;
        while((len = is.read(buffer)) != -1) {
            os.write(buffer,0,len);
            
            if(Thread.currentThread().isInterrupted()) {
                Log.debug.log(TAG, "Canceling upload...");
                return;
            }
            
            writtenBytes += len;
            updateProgress(writtenBytes);
        }
        
        os.flush();
        
        networkStream.getResponseCode();
        
        os.close();
        is.close();
    }

    private void uploadDirectory(File file) {
        setMaxProgress(1);
        updateProgress(0);

        boolean success = file.makeDirs();
        
        updateProgress(1);
    }
    
    private void updateFileInfo(String name, int index, int totalFiles) {
        handler.post(() -> {
            view.name.setText(name);
            view.progress.setText((index) + "/" + totalFiles);
        });
    }
    
    private void setMaxProgress(double maxProgress) {
        handler.post(() -> view.progressIndicator.setMax((int) maxProgress));
    }

    private void updateProgress(double progress) {
        int max = view.progressIndicator.getMax();
        double oldProgress = view.progressIndicator.getProgress();
        double oldPercentage = Math.floor(oldProgress / max * 100);
        
        int percentage = (int)Math.floor(progress / max * 100);
        if(percentage > oldPercentage) {
            handler.post(() -> {
                view.progressIndicator.setProgress((int) progress);
                view.progressPercent.setText(percentage + "%");
            });
        }
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
