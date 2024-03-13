package com.abiddarris.lanfileviewer.settings.material3;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.app.Dialog;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.runnables.DeleteRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.Thumbnails;
import com.abiddarris.preferences.DialogPreference;
import com.abiddarris.preferences.PreferenceFragment;
import com.gretta.util.log.Log;

import java.util.ArrayList;
import java.util.List;

public class DeleteThumbnailsCachePreference extends DialogPreference {
    
    public static final String TAG = Log.getTag(DeleteThumbnailsCachePreference.class);
    
    private List<File> files;
    private File cache;
    
    public DeleteThumbnailsCachePreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }
    
    @Override
    protected Dialog onCreateDialog(DialogFragment fragment) {
        cache = getTarget();
        
        files = new ArrayList<>();
        Files.getFilesTree(files, cache);
        
        try {
            cache.updateData((e) -> {}, REQUEST_GET_FILES_TREE_SIZE)
                .get();    
        } catch (Exception e) {
            Log.err.log(TAG, e);
        }
        
        long size = cache.getFilesTreeSize();
        
        String message = String.format(getContext().getString(R.string.delete_confirmation),
            getContext().getString(getText()).toLowerCase(), Files.formatSize(size));
        
        AlertDialog dialog = (AlertDialog) super.onCreateDialog(fragment);
        dialog.setCancelable(false);
        dialog.setMessage(message);
        
        return dialog;
    }
    
    protected File getTarget() {
        return FileSource.createFile(getContext(), Thumbnails.getThumbnailsCacheFolder(getContext()));
    }
    
    protected int getText() {
        return R.string.thumbnails_cache;
    }

    @Override
    protected void onSave() {
        super.onSave();

        DeleteRunnable runnable = new DeleteRunnable(FileSource.toPointers(cache),
            Files.formatFromItems(getContext(), files.toArray(new File[0])));
        new ActionDialog(null, runnable)
            .show(getFragment().getParentFragmentManager(),null);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDialogDestroy() {
        super.onDialogDestroy();
        
        FileSource.freeFiles(files);
        FileSource.freeFiles(cache);
    }
    
}
