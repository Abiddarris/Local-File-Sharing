package com.abiddarris.lanfileviewer.actions.runnables;

import android.content.Context;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.explorer.Explorer;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.CacheManager;
import com.gretta.util.Randoms;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadManager extends CacheManager<FilePointer, FilePointer>{
    
    private static DownloadManager downloadManager;
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Explorer explorer;
    private Context context;
    
    public DownloadManager(Context context) {
        this.context = context;
    }
    
    public void get(FilePointer item, Explorer explorer, OnDownloadedListener listener) {
        executor.submit(new BaseRunnable(c -> {
            this.explorer = explorer;        
                    
            FilePointer file = get(item);
            listener.onDownloaded(file);
        }));
    }
    
    @Override
    protected FilePointer create(FilePointer itemPointer) {
        String folderName = Randoms.getRandomString()
            .get(12);
        
        File downloadFolder = getDownloadFolder(context);
        File dest = FileSource.createFile(context, downloadFolder, folderName);
       
        FileSource.freeFiles(downloadFolder);
        
        dest.makeDirs();
        
        Log.debug.log(TAG, "download dest : " + dest.getPath());
        AtomicBoolean done = new AtomicBoolean(false);
        
        ApplicationCore.getMainHandler()
            .post(c -> {
                new ActionDialog(explorer,  new DownloadAndOpenRunnable(
                    new FilePointer[]{itemPointer}, dest.getFilePointer(), done))
                    .show(explorer.getFragment().getParentFragmentManager(), null);
            });
        
        while(!done.get()) {
        }
        
        File item = itemPointer.get();
        FilePointer file = FileSource.getFilePointer(
            context, dest, item.getName());
        Log.debug.log(TAG, "downloaded file : " + file);
        
        FileSource.freeFiles(item, dest);
        
        return file;
    }
    
    @Override
    protected boolean validate(FilePointer key, FilePointer value) {
        return true;
    }
    
    public static interface OnDownloadedListener {
        void onDownloaded(FilePointer item);
    }
    
    private class DownloadAndOpenRunnable extends DownloadRunnable {
    
        private final String TAG = Log.getTag(DownloadAndOpenRunnable.class);
        
        private AtomicBoolean done;
        
        DownloadAndOpenRunnable(FilePointer[] item, FilePointer dest, AtomicBoolean done) {
            super(item, dest);
        
            this.done = done;
        }
    
        @Override
        public void onExecute(BaseRunnable context) throws Exception {
            super.onExecute(context);
           
            done.set(true);
        }
    }
    
    public static File getDownloadFolder(Context context) {
        File cache = FileSource.getCacheDirectory(context);
        File downloadFolder = FileSource.createFile(context, cache , "download");
        
        FileSource.freeFiles(cache);
        
        return downloadFolder;
    }
    
    public static DownloadManager getDownloadManager(Context context) {
        if(downloadManager == null) {
            downloadManager = new DownloadManager(context);
        }
        return downloadManager;
    }
    
    
    
}
