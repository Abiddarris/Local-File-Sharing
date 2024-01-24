package com.abiddarris.lanfileviewer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Glide;
import com.gretta.util.log.Log;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class ThumbnailManager extends CacheManager<String, File> {
    
    private Context context;
    private File thumbnailDatas;
    private int lastThumbnailSize;
    
    public ThumbnailManager(Context context) {
        this.context = context;
        
        File thumbnailsFolder = Thumbnails.getThumbnailsCacheFolder(context);
        thumbnailDatas = new File(thumbnailsFolder, "thumbnail-datas");
    }
    
    @Override
    protected File create(String key) {
        try {
            Timer timer = new Timer();
            File file = new File(key);
            Bitmap bitmap= Glide.with(context)
                .asBitmap()
                .priority(Priority.IMMEDIATE)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(file)
                .submit(96,96)
                .get();
            
            Log.debug.log(TAG, "time takes to create thumbnail : " + timer.reset() + " ms");
            
            File cacheFolder = Thumbnails.getThumbnailsCacheFolder(context);
            cacheFolder.mkdirs();
            
            File cache = File.createTempFile("thumb-", "-" + file.length(), cacheFolder);
        
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(cache));
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        
            outputStream.close();
            
            Log.debug.log(TAG, "time takes to write thumbnail : " + timer.reset() + " ms");
            
            return cache;
        } catch (ExecutionException | InterruptedException | IOException e) {
            Log.err.log(TAG, e);
            return null;
        }
    }
    
    @Override
    protected boolean validate(String key, File value) {
        File file = new File(key);
        String thumbnailName = value.getName();
        int startSize = thumbnailName.lastIndexOf("-") + 1;
        String sizeString = thumbnailName.substring(startSize);
          
        long size = Long.valueOf(sizeString);
        if(size == file.length()) {
            return true;
        }
        
        value.delete();
        return false;
    }
    
    @Override
    protected Map<String, File> onLoad() {
        Timer timer = new Timer();
        Map<String, File> thumbnails;
        
        if(!thumbnailDatas.exists()) {
            return new HashMap<>();
        }
        
        try(ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(thumbnailDatas)))){
            thumbnails = (HashMap) inputStream.readObject();
            for(String path : thumbnails.keySet()) {
                File thumbnail = thumbnails.get(path);
                if(!thumbnail.exists()) {
                    Log.err.log(TAG, thumbnail  + " does not exist anymore");
                }
            }
            
            for(File file : Thumbnails.getThumbnailsCacheFolder(context).listFiles()) {
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
            return new HashMap<>();
        }
        
        Log.debug.log(TAG, "Loading thumbnail datas takes " + timer.reset() + " ms");
        
        return thumbnails;
    }

    @Override
    protected void onSave(Map<String, File> thumbnails) throws Exception {
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
    
    @Override
    public void clear() {
        super.clear();
    }
}
