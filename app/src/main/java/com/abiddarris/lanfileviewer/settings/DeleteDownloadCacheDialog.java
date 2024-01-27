package com.abiddarris.lanfileviewer.settings;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.runnables.DownloadManager;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;

public class DeleteDownloadCacheDialog extends DeleteThumbnailsCacheDialog{
    
    @Override
    protected int getText() {
        return R.string.download_cache;
    }
    
    @Override
    protected File getTarget() {
        return DownloadManager.getDownloadFolder(getContext());
    }
    
    
}
