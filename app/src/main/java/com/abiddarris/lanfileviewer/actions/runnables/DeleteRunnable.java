package com.abiddarris.lanfileviewer.actions.runnables;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.List;

public class DeleteRunnable extends ActionRunnable {

    private File[] items;
    private String title;
    
    public static final String TAG = Log.getTag(DeleteRunnable.class);

    public DeleteRunnable(File[] items, String title) {
        this.items = items;
        this.title = title;
    }
    
    @Override
    public String getTitle() {
        return getDialog().getString(R.string.deleting) + " " + title;
    }
    

    @Override
    public void onExecute(BaseRunnable context) throws Exception {
        prepare();

        List<File> files = new ArrayList<>();
        for(File item : items) {
        	Files.getFilesTree(files, item);
        }
        
        start();
        
        int index = 1;
        for(File file : files) {
            if(Thread.currentThread().isInterrupted()) {
                Log.debug.log(TAG, "Canceling delete");
                return;
            }
            file.updateDataSync(REQUEST_IS_DIRECTORY, REQUEST_IS_FILE);
        	if(file.isDirectory()) continue;
            
            updateFileInfo(file.getName(), index, files.size());
            index++;
            
            setMaxProgress(1);
            
            boolean success = file.delete();
            if(!success) {
                Log.err.log(TAG, "error while deletes files");
                return;
            }
            
            updateProgress(1);
        }
        
        for(int i = files.size() - 1; i >= 0; i--) {
            if(Thread.currentThread().isInterrupted()) {
                Log.debug.log(TAG, "Canceling delete");
                return;
            }
            
            File file = files.get(i);
            
        	if(file.isFile()) continue;
            
            updateFileInfo(file.getName(), index, files.size());
            index++;
            
            setMaxProgress(1);
            
            boolean success = file.delete();
            if(!success) {
                Log.err.log(TAG, "error while deletes folders");
                return;
            }
            
            updateProgress(1);
        }
    }
}
