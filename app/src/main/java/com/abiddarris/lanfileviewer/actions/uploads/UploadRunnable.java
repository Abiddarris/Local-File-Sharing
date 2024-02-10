package com.abiddarris.lanfileviewer.actions.uploads;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.content.Context;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.file.sharing.NetworkOutputStream;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.gretta.util.log.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadRunnable extends ActionRunnable {

    private File dest;
    private File[] items;
    private FileSource destSource;
    
    public static final String TAG = Log.getTag(UploadRunnable.class);
    
    public UploadRunnable(FileSource destSource, FilePointer dest, FilePointer[] items) {
        this.destSource = destSource;
        this.dest = dest.get();
        
        this.items = new File[items.length];
        for(int i = 0; i < items.length; i++) {
            this.items[i] = items[i].get();
        }
    }
    
    @Override
    public String getTitle() {
        Context context = getDialog()
            .getContext();
        
        String copy = context.getString(R.string.uploading);
        String formattedItems = Files.formatFromItems(context, items);
        return String.format("%s %s", copy, formattedItems);
    }

    @Override
    public void onExecute(BaseRunnable context) throws Exception {
        prepare();

        List<File> files = new ArrayList<>();
        for (File file : items) {
            Files.getFilesTree(files, file);
        }

        start();

        String parent = items[0].getParent();
        for (int i = 0; i < files.size(); ++i) {
            if(Thread.currentThread().isInterrupted()) {
                Log.debug.log(TAG, "Canceling upload...");
                return;
            }
            
            File originalFile = files.get(i);
            originalFile.updateDataSync(REQUEST_GET_LENGTH, REQUEST_GET_MIME_TYPE);
            
            String localPath = originalFile.getPath()
                .replace(parent, "");
            
            File destFile = getDialog()
                .getFile(destSource, originalFile, dest.getPath() + localPath);
           
            if(destFile == null) continue;
            
            updateFileInfo(originalFile.getName(), i + 1, files.size());
            
            if (originalFile.isDirectory()) {
                uploadDirectory(destFile);
            } else {
                uploadFile(originalFile, destFile);
            }
            
            FileSource.freeFiles(originalFile, destFile);
        }
    }

    private void uploadFile(File src, File dest) throws IOException {
        setMaxProgress(src.length());
        
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

        boolean success = file.makeDirs();
        
        updateProgress(1);
    }
    
    
}
