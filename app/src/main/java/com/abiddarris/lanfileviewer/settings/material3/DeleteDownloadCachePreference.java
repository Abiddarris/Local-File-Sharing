package com.abiddarris.lanfileviewer.settings.material3;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.runnables.DownloadManager;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.preferences.PreferenceFragment;

public class DeleteDownloadCachePreference extends DeleteThumbnailsCachePreference {
    
    public DeleteDownloadCachePreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }
    
    @Override
    protected int getText() {
        return R.string.download_cache;
    }
    
    @Override
    protected File getTarget() {
        return DownloadManager.getDownloadFolder(getContext());
    }
    
}
