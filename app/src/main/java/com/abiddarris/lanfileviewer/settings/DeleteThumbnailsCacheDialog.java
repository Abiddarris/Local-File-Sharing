package com.abiddarris.lanfileviewer.settings;

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

public class DeleteThumbnailsCacheDialog extends DialogFragment {
    
    public static final String TAG = Log.getTag(DeleteThumbnailsCacheDialog.class);
    
    private List<File> files;
    private File cache;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        java.io.File file = Thumbnails.getThumbnailsCacheFolder(getContext());
        cache = FileSource.getDefaultLocalSource(getContext())
            .getFile(file.getPath());
        
        files = new ArrayList<>();
        Files.getFilesTree(files, cache);
        
        long size = cache.getFilesTreeSize();
        
        String message = String.format(getString(R.string.delete_confirmation),
            getString(R.string.thumbnails_cache).toLowerCase(), Files.formatSize(size));
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setMessage(message)
            .setNeutralButton(R.string.cancel, (p1,p2) -> {})
            .setPositiveButton(R.string.ok, (p1,p2) -> delete())
            .create();
        
        return dialog;
    }

    private void delete() {
        DeleteRunnable runnable = new DeleteRunnable(new File[]{cache},
            Files.formatFromItems(getContext(), files.toArray(new File[0])));
        new ActionDialog(null, runnable)
            .show(getParentFragmentManager(),null);
    }
    
}
