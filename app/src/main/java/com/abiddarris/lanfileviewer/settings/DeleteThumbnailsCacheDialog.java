package com.abiddarris.lanfileviewer.settings;

import static com.abiddarris.common.android.preferences.DialogPreference.PREFERENCE;
import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.os.Bundle;

import com.abiddarris.common.android.dialogs.BaseDialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.runnables.DeleteRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.Thumbnails;
import com.abiddarris.common.android.preferences.DialogPreference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gretta.util.log.Log;

import java.util.ArrayList;
import java.util.List;

public class DeleteThumbnailsCacheDialog extends BaseDialogFragment<Void> {
    
    public static final String TAG = Log.getTag(DeleteThumbnailsCacheDialog.class);
    
    private List<File> files;
    private File cache;

    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        super.onCreateDialog(builder, savedInstanceState);
        setCancelable(false);

        DialogPreference preference = getVariable(PREFERENCE);
        if (preference == null) {
            dismiss();
            return;
        }

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
        
        String message = String.format(
                getString(R.string.delete_confirmation),
                getString(getText()).toLowerCase(), Files.formatSize(size)
        );

        builder.setMessage(message)
                .setTitle(preference.getTitle())
                .setNegativeButton(android.R.string.cancel, (d, d2) -> {})
                .setPositiveButton(android.R.string.ok, (d, d2) -> {
                    DeleteRunnable runnable = new DeleteRunnable(FileSource.toPointers(cache),
                            Files.formatFromItems(getContext(), files.toArray(new File[0])));
                    new ActionDialog(null, runnable)
                            .show(getParentFragment().getParentFragmentManager(),null);
                });
    }
    
    protected File getTarget() {
        return FileSource.createFile(getContext(), Thumbnails.getThumbnailsCacheFolder(getContext()));
    }
    
    protected int getText() {
        return R.string.thumbnails_cache;
    }

    @Override
    public void onDestroy() {
        FileSource.freeFiles(files);
        FileSource.freeFiles(cache);

        super.onDestroy();
    }
    
}
