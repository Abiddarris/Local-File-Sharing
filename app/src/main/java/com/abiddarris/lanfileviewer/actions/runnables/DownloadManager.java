package com.abiddarris.lanfileviewer.actions.runnables;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.content.Context;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.explorer.Explorer;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.sharing.NetworkFileSource;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.CacheManager;
import com.gretta.util.Randoms;
import com.gretta.util.log.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadManager extends CacheManager<FilePointer, FilePointer>{
    
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Explorer explorer;
    private Context context;
    private NetworkFileSource source;
    
    public DownloadManager(Context context, NetworkFileSource source) {
        this.context = context;
        this.source = source;
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
    

    @Override
    protected void onSave(Map<FilePointer, FilePointer> caches) throws Exception {
        super.onSave(caches);
        
        File downloadFolder = getDownloadFolder(context);
        File downloadData = FileSource.createFile(context, downloadFolder, source.getServerId() + "-data");
        
        FileSource.freeFiles(downloadFolder);
        
        BufferedWriter writer = new BufferedWriter(downloadData.newWriter());
        
        FileSource.freeFiles(downloadData);
        
        for(FilePointer key : caches.keySet()) {
            FilePointer value = caches.get(key);
            
            writer.write(key.getPath());
            writer.newLine();
            writer.write(value.getPath());
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    @Override
    protected Map<FilePointer, FilePointer> onLoad() throws Exception {
        Map<FilePointer, FilePointer> caches = new HashMap<>();
        
        File downloadFolder = getDownloadFolder(context);
        File downloadData = FileSource.createFile(context, downloadFolder, source.getServerId() + "-data");
        
        FileSource.freeFiles(downloadFolder);
        
        downloadData.updateDataSync(REQUEST_EXISTS);
        if(!downloadData.exists()) return caches;
        
        BufferedReader reader = new BufferedReader(downloadData.newReader());
        
        FileSource.freeFiles(downloadData);
        
        String data;
        while((data = reader.readLine()) != null) {
            FilePointer key = source.getFilePointer(data);
            FilePointer value = source.getFilePointer(
                reader.readLine());
            
            caches.put(key, value);
        }
        reader.close();
        
        return caches;
    }
}
