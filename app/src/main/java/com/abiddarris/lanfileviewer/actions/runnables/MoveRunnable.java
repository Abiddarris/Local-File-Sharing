package com.abiddarris.lanfileviewer.actions.runnables;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.content.Context;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.List;

public class MoveRunnable extends ActionRunnable {

    private File[] items;
    private FileSource source;
    private File dest;

    public MoveRunnable(File[] items, File dest) {
        this.items = items;
        this.dest = dest;
        this.source = dest.getSource();
    }
    
    @Override
    public String getTitle() {
        Context context = getDialog()
            .getContext();
        
        String copy = context.getString(R.string.moving);
        String formattedItems = Files.formatFromItems(context, items);
        return String.format("%s %s", copy, formattedItems);
    }

    @Override
    public void onExecute(BaseRunnable context) throws Exception {
        prepare();

        List<File> files = new ArrayList<>();
        for (File item : items) {
            Files.getFilesTree(files, item);
        }

        start();

        File parent = items[0].getParentFile();
        for (int i = 0; i < files.size(); ++i) {
            if (Thread.currentThread().isInterrupted()) {
                Log.err.log(getTag(), "Moving interrupted");
                return;
            }

            File originalFile = files.get(i);
            originalFile.updateDataSync(REQUEST_IS_DIRECTORY);

            String localPath = originalFile.getPath().replace(parent.getPath(), "");

            File destFile = getDialog()
                .getFile(source, originalFile, dest.getPath() + localPath);
            
            if (destFile == null) continue;
            
            updateFileInfo(originalFile.getName(), i + 1, files.size());

            if (originalFile.isDirectory()) {
                moveDirectory(destFile);
            } else {
                moveFile(originalFile, destFile);
            }
            
            FileSource.freeFile(destFile);
        }
        
        for (int i = files.size() - 1; i >= 0; i--) {
            File originalFile = files.get(i);
            if(originalFile.isDirectory()) {
                originalFile.delete();
            }
            FileSource.freeFile(originalFile);
        }
    }
    
    private void moveFile(File originalFile, File destFile) {
        File.Progress progress = originalFile.move(destFile);
        setMaxProgress(progress.getSize());
        
        while(!progress.isCompleted()) {
            if(Thread.currentThread().isInterrupted()) {
                progress.setCancel(true);
                
                break;
            }
            Log.debug.log(getTag(), progress.getCurrentProgress());
            updateProgress(progress.getCurrentProgress());
        }
        updateProgress(progress.getCurrentProgress());
        Log.debug.log(getTag(), progress.getCurrentProgress());
        
        if(progress.getException() == null) return;
       
        Log.err.log(getTag(), progress.getException());
    }

    private void moveDirectory(File file) {
        setMaxProgress(1);

        boolean success = file.makeDirs();
        
        updateProgress(1);
    }
    
}
