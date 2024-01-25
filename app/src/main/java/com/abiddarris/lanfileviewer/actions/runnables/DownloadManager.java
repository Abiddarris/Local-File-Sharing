package com.abiddarris.lanfileviewer.actions.runnables;

import android.content.Context;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.explorer.Explorer;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.CacheManager;
import com.gretta.util.Randoms;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadManager extends CacheManager<File, File>{
    
    private static DownloadManager downloadManager;
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Explorer explorer;
    private Context context;
    
    public DownloadManager(Context context) {
        this.context = context;
    }
    
    public void get(File item, Explorer explorer, OnDownloadedListener listener) {
        executor.submit(new BaseRunnable(c -> {
            this.explorer = explorer;        
                    
            File file = get(item);
            listener.onDownloaded(file);
        }));
    }
    
    @Override
    protected File create(File item) {
        java.io.File downloadFolder = new java.io.File(context.getCacheDir(), "download");
        String folderName = Randoms.getRandomString()
            .get(12);
        
        java.io.File subFolder = new java.io.File(downloadFolder, folderName);
        subFolder.mkdirs();
        
        FileSource source = FileSource.getDefaultLocalSource(context);
        File dest = source.getFile(subFolder.getAbsolutePath());
        
        Log.debug.log(TAG, "download dest : " + dest.getPath());
        AtomicBoolean done = new AtomicBoolean(false);
        
        ApplicationCore.getMainHandler()
            .post(c -> {
                new ActionDialog(explorer,  new DownloadAndOpenRunnable(new File[]{item}, dest, done))
                    .show(explorer.getFragment().getParentFragmentManager(), null);
            });
        
        while(!done.get()) {
        }
        
        File file = source.getFile(dest.getPath() + "/" + item.getName());
        Log.debug.log(TAG, "downloaded file : " + file);
        
        return file;
    }
    
    @Override
    protected boolean validate(File key, File value) {
        return true;
    }
    
    public static interface OnDownloadedListener {
        void onDownloaded(File item);
    }
    
    private class DownloadAndOpenRunnable extends DownloadRunnable {
    
        private final String TAG = Log.getTag(DownloadAndOpenRunnable.class);
        
        private AtomicBoolean done;
        private File item;
        private File dest;
    
        DownloadAndOpenRunnable(File[] item, File dest, AtomicBoolean done) {
            super(item, dest);
        
            this.item = item[0];
            this.dest = dest;
            this.done = done;
        }
    
        @Override
        public void onExecute(BaseRunnable context) throws Exception {
            super.onExecute(context);
           
            done.set(true);
        }
    }
    
    public static DownloadManager getDownloadManager(Context context) {
        if(downloadManager == null) {
            downloadManager = new DownloadManager(context);
        }
        return downloadManager;
    }
    
    
    
}
