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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadRunnable extends ActionRunnable {

    private File[] items;
    private File dest;
    
    public static final String TAG = Log.getTag(DownloadRunnable.class);
    
    public DownloadRunnable(FilePointer[] items, FilePointer dest) {
        this.items = FileSource.toFiles(items);
        this.dest = dest.get();
    }
    
    @Override
    public String getTitle() {
        Context context = getDialog()
            .getContext();
        
        String copy = context.getString(R.string.downloading);
        String formattedItems = Files.formatFromItems(context, items);
        return String.format("%s %s", copy, formattedItems);
    }

    @Override
    public void onExecute(BaseRunnable context) throws Exception {
        prepare();
        
        FileSource source = dest.getSource();

        List<File> files = new ArrayList<>();
        for (File file : items) {
            Files.getFilesTree(files, file);
        }

        start();

        String parent = items[0].getParent();
        for (int i = 0; i < files.size(); ++i) {
            if (Thread.currentThread().isInterrupted()) {
                Log.debug.log(TAG, "Canceling downloading...");
                return;
            }

            File originalFile = files.get(i);
            originalFile.updateDataSync(REQUEST_IS_DIRECTORY, REQUEST_GET_LENGTH);
            
            String localPath = originalFile.getPath()
                .replace(parent, "");

            File destFile = getDialog()
                .getFile(source, originalFile, dest.getPath() + localPath);
            
            if (destFile == null) continue;
            
            updateFileInfo(originalFile.getName(), i + 1, files.size());

            if (originalFile.isDirectory()) {
                downloadDirectory(destFile);
            } else {
                downloadFile(originalFile, destFile);
            }
            
            FileSource.freeFiles(originalFile, destFile);
        }
        FileSource.freeFiles(dest);
    }
    
    public void downloadDirectory(File file) {
    	setMaxProgress(1);
        
        file.makeDirs();
        
        updateProgress(1);
    }
    
    public void downloadFile(File originalFile, File dest) throws IOException {
    	setMaxProgress(originalFile.length());
        
        BufferedInputStream is = new BufferedInputStream(originalFile.newInputStream());
        BufferedOutputStream os = new BufferedOutputStream(dest.newOutputStream());
        byte[] buf = new byte[8 * 1024];
        int len;
        long writtenBytes = 0;
        while((len = is.read(buf)) != -1) {
            if(Thread.currentThread().isInterrupted()) {
                Log.debug.log(TAG, "Canceling download...");
                return;
            }
            
            
        	os.write(buf,0,len);
            writtenBytes += len;
            
            updateProgress(writtenBytes);
        }
        os.flush();
        os.close();
        is.close();
    }
}
