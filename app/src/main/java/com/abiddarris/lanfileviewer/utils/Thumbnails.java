package com.abiddarris.lanfileviewer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gretta.util.log.Log;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Thumbnails {
    
    public static final String TAG = Log.getTag(Thumbnails.class);
    
    private static final Map<String,File> thumbnails = new HashMap<>();
    
    @Nullable
    public static File getThumbnail(Context context, File file) {
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
            
            File cacheFolder = new File(context.getCacheDir(), "thumbnail-cache");
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
    
}
