package com.abiddarris.lanfileviewer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gretta.util.log.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Thumbnails {
    
    public static final String TAG = Log.getTag(Thumbnails.class);
    private static final int THUMBNAIL_DATAS_SAVER_DELAY = 1000 * 60;

    private static File thumbmailsFolder;
    private static int lastThumbnailSize;
    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static Map<String,File> thumbnails; 
    
    @Nullable
    public static File getThumbnail(Context context, File file) {
        if(thumbnails == null) {
            loadThumbnailDatas(context);
        }
        
        Timer timer = new Timer();
        File cache = thumbnails.get(file.getPath());
        if(cache != null) {
            String thumbnailName = cache.getName();
            int startSize = thumbnailName.lastIndexOf("-") + 1;
            String sizeString = thumbnailName.substring(startSize);
            
            long size = Long.valueOf(sizeString);
            if(size == file.length()) {
                Log.debug.log(TAG, "returning from cache with time " + timer.reset() + " ms");
                return cache;
            }
            thumbnails.remove(file.getPath());
            cache.delete();
        }
        Log.debug.log(TAG, "Checking cache takes " + timer.reset() + " ms");
        try {
            Bitmap bitmap= Glide.with(context)
                .asBitmap()
                .priority(Priority.IMMEDIATE)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(file)
                .submit(96,96)
                .get();
            
            Log.debug.log(TAG, "time takes to create thumbnail : " + timer.reset() + " ms");
            
            File cacheFolder = getThumbnailsCacheFolder(context);
            cacheFolder.mkdirs();
            
            cache = File.createTempFile("thumb-", "-" + file.length(), cacheFolder);
        
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(cache));
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        
            outputStream.close();
            
            thumbnails.put(file.getPath(), cache);

            Log.debug.log(TAG, "time takes to write thumbnail : " + timer.reset() + " ms");
            
            return cache;
        } catch (ExecutionException | InterruptedException | IOException e) {
            Log.err.log(TAG, e);
            return null;
        }
    }
    
    private static void loadThumbnailDatas(Context context)  {
        Timer timer = new Timer();
        
        File thumbnailsFolder = getThumbnailsCacheFolder(context);
        File thumbnailDatas = new File(thumbmailsFolder, "thumbnail-datas");
        if(!thumbnailDatas.exists()) {
            thumbnails = new HashMap<>();
            executor.scheduleWithFixedDelay(new ThumbnailDatasSaver(thumbnailDatas), 0, 1, TimeUnit.MINUTES);
            return;
        }
        
        try(ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(thumbnailDatas)))){
            thumbnails = (HashMap) inputStream.readObject();
            for(String path : thumbnails.keySet()) {
                File thumbnail = thumbnails.get(path);
                if(!thumbnail.exists()) {
                    Log.err.log(TAG, thumbnail  + " does not exist anymore");
                }
            }
            
            for(File file : getThumbnailsCacheFolder(context).listFiles()) {
                boolean exists = false;
                for(File thumbnail : thumbnails.values()) {
                    if(thumbnail.getPath().equalsIgnoreCase(file.getPath()) || thumbnailDatas.getPath().equalsIgnoreCase(file.getPath())) {
                        exists = true;
                        break;
                    }
                }
                if(!exists) {
                    boolean success = file.delete();
                    if(!success) {
                        Log.err.log(TAG, "Failed to delete " + file);
                    }
                    Log.debug.log(TAG, file + " does not associated with any file");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.err.log(TAG, e);
            thumbnails = new HashMap<>();
        }
        
        Log.debug.log(TAG, "Loading thumbnail datas takes " + timer.reset() + " ms");
        
        executor.scheduleWithFixedDelay(new ThumbnailDatasSaver(thumbnailDatas), 0, 1, TimeUnit.MINUTES);
    }

    private static void saveThumbnailDatas(File thumbnailDatas) throws IOException {
        Timer timer = new Timer();
    	if(lastThumbnailSize == thumbnails.size()) {
            Log.debug.log(TAG, "Skipping save operation");
            return;
        }
        Log.debug.log(TAG, "checking save operation is needed takes " + timer.reset() + " ms");
        
        File temp = File.createTempFile("thumbnailData-", "", thumbnailDatas.getParentFile());
        try(ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(temp)))){
            outputStream.writeObject(thumbnails);
            outputStream.flush();
        } 
        
        lastThumbnailSize = thumbnails.size();
        
        boolean success = thumbnailDatas.delete();

        success = temp.renameTo(thumbnailDatas);
        if(!success) {
            throw new IOException("Cannot save thumbnail datas! unable to rename " + temp.getPath() + " to " + thumbnailDatas.getPath());
        }
        
        Log.debug.log(TAG, "Save operation takes " + timer.reset() + " ms");
    }
    
    private static File getThumbnailsCacheFolder(Context context) {
        if(thumbmailsFolder == null) {
            thumbmailsFolder = new File(context.getCacheDir(), "thumbnail-cache");
        }
    	return thumbmailsFolder;
    }
    
    private static class ThumbnailDatasSaver extends BaseRunnable {
        
        private File thumbnailDatas;
        
        public ThumbnailDatasSaver(File thumbnailDatas) {
            this.thumbnailDatas = thumbnailDatas;
        }
        
        @Override
        public void onExecute(BaseRunnable context) throws Exception {
            saveThumbnailDatas(thumbnailDatas);
        }
        
    }
    
}
