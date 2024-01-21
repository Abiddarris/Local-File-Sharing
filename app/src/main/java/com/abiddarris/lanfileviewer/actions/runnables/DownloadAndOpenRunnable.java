package com.abiddarris.lanfileviewer.actions.runnables;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import androidx.core.content.FileProvider;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.gretta.util.Randoms;
import com.gretta.util.log.Log;

public class DownloadAndOpenRunnable extends DownloadRunnable {
    
    public static final String TAG = Log.getTag(DownloadAndOpenRunnable.class);
    
    private File item;
    private File dest;
    
    DownloadAndOpenRunnable(File[] item, File dest) {
        super(item, dest);
        
        this.item = item[0];
        this.dest = dest;
    }
    
    @Override
    public void onExecute(BaseRunnable context) throws Exception {
        super.onExecute(context);
        
        File file = dest.getSource()
            .getFile(dest.getPath() + "/" + item.getName());
        Log.debug.log(TAG, "downloaded file : " + file);
        
        Context androidContext = getDialog()
            .getContext();
        
        Uri uri = FileProvider.getUriForFile(androidContext, 
            androidContext.getApplicationContext().getPackageName() + ".provider",
            new java.io.File(file.getAbsolutePath()));
        
        ApplicationCore.getMainHandler()
            .post((c) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, file.getMimeType());
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                androidContext.startActivity(intent);
            });
    }
    
    public static DownloadAndOpenRunnable create(Context context, File item) {
        java.io.File downloadFolder = new java.io.File(context.getCacheDir(), "download");
        String folderName = Randoms.getRandomString()
            .get(12);
        
        java.io.File subFolder = new java.io.File(downloadFolder, folderName);
        subFolder.mkdirs();
        
        FileSource source = FileSource.getDefaultLocalSource(context);
        File dest = source.getFile(subFolder.getAbsolutePath());
        
        Log.debug.log(TAG, "download dest : " + dest.getPath());
        
        return new DownloadAndOpenRunnable(new File[]{item}, dest);
    }
    
}
