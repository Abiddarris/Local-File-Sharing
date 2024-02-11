package com.abiddarris.lanfileviewer.actions.runnables;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.content.Context;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.List;

public class CopyRunnable extends ActionRunnable {

    private File dest;
    private File[] items;
    private FileSource source;
    private List<File> files;
    
    public static final String TAG = Log.getTag(CopyRunnable.class);

    public CopyRunnable(FilePointer pointer, FilePointer[] items) {
        this.dest = pointer.get();
        this.source = dest.getSource();
        this.items = FileSource.toFiles(items);
    }
    
    @Override
    public String getTitle() {
        Context context = getDialog()
            .getContext();
        
        String copy = context.getString(R.string.copying);
        String formattedItems = Files.formatFromItems(context, items);
        return String.format("%s %s", copy, formattedItems);
    }
    
    @Override
    public void onExecute(BaseRunnable context) throws Exception {
        prepare();
        
        files = new ArrayList<>();
        for (File file : items) {
            Files.getFilesTree(files, file);
        }

        start();

        String parent = items[0].getParent();
        for (int i = 0; i < files.size(); ++i) {
            if(Thread.currentThread().isInterrupted()) {
                Log.debug.log(TAG, "Canceling copy...");
                return;
            }
            
            File originalFile = files.get(i);
            originalFile.updateDataSync(REQUEST_GET_LENGTH, REQUEST_IS_DIRECTORY);
            
            String localPath = originalFile.getPath()
                .replace(parent, "");
            
            File destFile = getDialog()
                .getFile(source, originalFile, dest.getPath() + localPath);
            
            if(destFile == null) continue;
            
            updateFileInfo(originalFile.getName(), i + 1, files.size());
            
            if (originalFile.isDirectory()) {
                copyDirectory(destFile);
            } else {
                copyFile(originalFile, destFile);
            }
            
            FileSource.freeFiles(destFile);   
        }
    }

    private void copyFile(File originalFile, File destFile) {
        File.Progress progress = originalFile.copy(destFile);
        setMaxProgress(progress.getSize());
        
        Log.debug.log(getTag(), "original file " + originalFile.length());
        Log.debug.log(getTag(), "progress max " + progress.getSize());
        
        while(!progress.isCompleted()) {
            if(Thread.currentThread().isInterrupted()) {
                progress.setCancel(true);
                break;
            }
            Log.debug.log(TAG, progress.getCurrentProgress());
            updateProgress(progress.getCurrentProgress());
        }
        Log.debug.log(TAG, progress.getCurrentProgress());
        updateProgress(progress.getCurrentProgress());
        
        if(progress.getException() == null) return;
       
        Log.err.log(TAG, progress.getException());
    }

    private void copyDirectory(File file) {
        setMaxProgress(1);
        
        boolean success = file.makeDirs();
        
        updateProgress(1);
    }
    
    @Override
    public void onFinalization() {
        super.onFinalization();
        
        FileSource.freeFiles(dest);
        FileSource.freeFiles(items);
        FileSource.freeFiles(files);
    }
    
}
