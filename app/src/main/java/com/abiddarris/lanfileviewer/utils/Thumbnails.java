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
    
    private static File thumbmailsFolder;
    private static ThumbnailManager manager;
    
    @Nullable
    public static File getThumbnail(Context context, File file) {
        if(manager == null) {
            manager = new ThumbnailManager(context);
        }
        return manager.get(file.getPath());
    }
    
    public static File getThumbnailsCacheFolder(Context context) {
        if(thumbmailsFolder == null) {
            thumbmailsFolder = new File(context.getCacheDir(), "thumbnail-cache");
        }
    	return thumbmailsFolder;
    }
    
}
