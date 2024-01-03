package com.abiddarris.lanfileviewer.explorer;
import android.os.Bundle;
import android.app.Dialog;
import com.abiddarris.lanfileviewer.R;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.runnables.DeleteRunnable;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gretta.util.log.Log;
import java.util.Arrays;

public class DeleteConfirmationDialog extends DialogFragment {
    
    private Explorer explorer;
    private File[] items;
    
    public static final String TAG = Log.getTag(DeleteConfirmationDialog.class);
    
    public DeleteConfirmationDialog(Explorer explorer, File[] items) {
        this.items = items;
        this.explorer = explorer;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        int fileCount = Files.getFilesCount(items);
        int folderCount = Files.getDirectoriesCount(items);
        
        int messageId;
        if(folderCount != 0 && fileCount != 0) {
            messageId = R.string.item_format;
        } else if(folderCount != 0) {
            messageId = folderCount == 1 ? R.string.one_folder_format : R.string.plural_folder_format;
        } else {
            messageId = fileCount == 1 ? R.string.one_file_format : R.string.plural_files_format;
        }
        
        String message = String.format(getString(messageId), items.length);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setMessage(getString(R.string.delete) + " " + message + '?')
            .setNeutralButton(R.string.cancel, (d, type) -> {})
            .setPositiveButton(R.string.delete, (d, type) -> {
                new ActionDialog(explorer, new DeleteRunnable(items, message))
                    .show(getParentFragmentManager(), null);
                explorer.setMode(explorer.navigateMode);
            })
            .create();
        return dialog;
    }
    
    
}