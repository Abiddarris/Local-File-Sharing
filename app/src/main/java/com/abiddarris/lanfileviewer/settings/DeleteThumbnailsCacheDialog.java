package com.abiddarris.lanfileviewer.settings;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.runnables.DeleteRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.Thumbnails;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DeleteThumbnailsCacheDialog extends DialogFragment {
    
    public static final String TAG = Log.getTag(DeleteThumbnailsCacheDialog.class);
    
    private List<File> files;
    private File cache;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
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
        
        String message = String.format(getString(R.string.delete_confirmation),
            getString(getText()).toLowerCase(), Files.formatSize(size));
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setMessage(message)
            .setCancelable(false)
            .setNeutralButton(R.string.cancel, (p1,p2) -> {})
            .setPositiveButton(R.string.ok, (p1,p2) -> delete())
            .create();
        
        return dialog;
    }
    
    protected File getTarget() {
        return FileSource.createFile(getContext(), Thumbnails.getThumbnailsCacheFolder(getContext()));
    }
    
    protected int getText() {
        return R.string.thumbnails_cache;
    }

    private void delete() {
        DeleteRunnable runnable = new DeleteRunnable(FileSource.toPointers(cache),
            Files.formatFromItems(getContext(), files.toArray(new File[0])));
        new ActionDialog(null, runnable)
            .show(getParentFragmentManager(),null);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        
        FileSource.freeFiles(files);
        FileSource.freeFiles(cache);
    }
    
}
