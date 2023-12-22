package com.abiddarris.lanfileviewer.actions.runnables;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.gretta.util.log.Log;
import java.util.List;
import java.util.ArrayList;
import com.abiddarris.lanfileviewer.file.Files;
import java.util.Set;
import com.abiddarris.lanfileviewer.file.File;

public class CopyRunnable extends ActionRunnable {

    private File dest;
    private File[] items;
    private FileSource source;
    
    public static final String TAG = Log.getTag(CopyRunnable.class);

    public CopyRunnable(File dest, Set<File> items) {
        this.source = dest.getSource();
        this.dest = dest;
        this.items = items.toArray(new File[0]);
    }
    
    @Override
    public String getTitle() {
        String title = getDialog().getString(R.string.copy_dialog_title);
        return String.format(title, items.length);
    }
    

    @Override
    public void onExecute() throws Exception {
        prepare();
        
        List<File> files = new ArrayList<>();
        for (File file : items) {
            Files.getFilesTree(files, file);
        }

        start();

        File parent = items[0].getParentFile();
        for (int i = 0; i < files.size(); ++i) {
            if(Thread.currentThread().isInterrupted()) {
                Log.debug.log(TAG, "Canceling copy...");
                return;
            }
            
            File originalFile = files.get(i);
            originalFile.updateDataSync();
            
            String localPath = originalFile.getPath()
                .replace(parent.getPath(), "");
            
            File destFile = getDialog()
                .getFile(source, dest.getPath() + localPath);
            destFile.updateDataSync();
            
            if(destFile == null) continue;
            
            updateFileInfo(originalFile.getName(), i + 1, files.size());
            
            if (originalFile.isDirectory()) {
                copyDirectory(destFile);
            } else {
                copyFile(originalFile, destFile);
            }
        }
    }

    private void copyFile(File originalFile, File destFile) {
        File.Progress progress = originalFile.copy(destFile);
        setMaxProgress(progress.getSize());
        updateProgress(0);
        
        while(!progress.isCompleted()) {
            if(Thread.currentThread().isInterrupted()) {
                progress.setCancel(true);
                break;
            }
            updateProgress(progress.getCurrentProgress());
        }
        
        if(progress.getException() == null) return;
       
        Log.err.log(TAG, progress.getException());
    }

    private void copyDirectory(File file) {
        setMaxProgress(1);
        updateProgress(0);

        boolean success = file.makeDirs();
        
        updateProgress(1);
    }
}
