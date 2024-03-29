package com.abiddarris.lanfileviewer;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.abiddarris.lanfileviewer.file.sharing.NetworkFileSource;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.abiddarris.lanfileviewer.utils.Theme;
import com.gretta.util.log.FilesLog;
import com.gretta.util.log.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApplicationCore extends Application {

    private static final String TAG = Log.getTag(ApplicationCore.class);

    private static ApplicationCore core;
    private static HandlerLogSupport mainHandler;

    private NetworkFileSource currentFileSource;

    @Override
    public void onCreate() {
        super.onCreate();

        core = this;

        Theme.apply(this);
        setupLogger();
    }

    public static ApplicationCore getApplication() {
        return core;
    }

    public static HandlerLogSupport getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
        }
        return mainHandler;
    }

    private void setupLogger() {
        File externalFolder = getExternalFilesDir(null);
        Log.err = new FilesLog(new File(externalFolder, "error.txt"));
        Log.out = new FilesLog(new File(externalFolder, "output.txt"));
        Log.debug = new FilesLog(new File(externalFolder, "debug.txt"));

        try {
            ((FilesLog) Log.err).open();
            ((FilesLog) Log.out).open();
            ((FilesLog) Log.debug).open();
            Log.debug.log(TAG, "Log setuped");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Thread.setDefaultUncaughtExceptionHandler(
                (t, e) -> {
                    if (e instanceof Exception) {
                        Log.err.log("Main Thread", (Exception) e);
                    } else {
                        Log.err.log("Main Thread", new Exception(e));
                    }
                    System.exit(1);
                });
        
    }

    public NetworkFileSource getCurrentFileSource() {
        return this.currentFileSource;
    }

    public void setCurrentFileSource(NetworkFileSource currentFileSource) {
        this.currentFileSource = currentFileSource;
    }
}
