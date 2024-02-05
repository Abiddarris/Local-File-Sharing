package com.abiddarris.lanfileviewer.explorer;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogDetailBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailDialog extends DialogFragment {

    private DialogDetailBinding binding;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private File[] items;

    public DetailDialog(File[] items) {
        this.items = items;
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogDetailBinding.inflate(getLayoutInflater());
        binding.name.setText(getName());
        
        setPath();
        setSize();
        setLastModified();
        setContaining();
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                        .setView(binding.getRoot())
                        .setPositiveButton(R.string.ok, (d, type) -> {})
                        .create();

        return dialog;
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        
        executor.shutdownNow();
    }
    
    
    private void setSize() {
    	if(items.length == 1 && items[0].isFile()) {
            binding.size.setText(
                Files.formatSize(items[0].length()));
        }
    }
    
    private void setContaining() {
    	if(items.length == 1 && items[0].isFile()) {
            hide(binding.containingText, binding.containing);
            return;
        }
        executor.submit(new BaseRunnable((c) -> {
            List<File> files = new ArrayList<>();
            for(File item : items) {
                if(Thread.currentThread().isInterrupted()) {
                    FileSource.freeFiles(files);   
                    return;   
                } 
            	Files.getFilesTree(files, item);
            }
                
            int filesCount = Files.getFilesCount(files);
            int directoriesCount = Files.getDirectoriesCount(files);
               
            FileSource.freeFiles(files);     
                    
            if(Thread.currentThread().isInterrupted()) return;   
                
            String fileFormat = getString(filesCount > 1 ? R.string.plural_files_format : R.string.one_file_format);
            String directoryFormat = getString(directoriesCount > 1 ? R.string.plural_folder_format : R.string.one_folder_format);
               
            directoryFormat = String.format(directoryFormat, directoriesCount);
            fileFormat = String.format(fileFormat, filesCount);
                
            String containingValue = "";   
            if(directoriesCount == 0 && filesCount != 0) {
                containingValue = fileFormat;
            } else if(directoriesCount != 0 && filesCount == 0) {
                containingValue = directoryFormat;
            } else if(directoriesCount != 0 && filesCount != 0) {
                containingValue = directoryFormat + ", " + fileFormat;
            }
                
            updateContainingUI(containingValue);
         
            if(Thread.currentThread().isInterrupted()) return;   
                
            long size = Files.getFilesTreeSize(items);
            String formattedSize = Files.formatSize(size);             
           
            if(Thread.currentThread().isInterrupted()) return;   
                
            updateSizeUI(formattedSize);
              
        }));
    }
    
    private void updateSizeUI(String size) {
    	getActivity().runOnUiThread(() -> binding.size.setText(size));
    }
    
    private void updateContainingUI(String containing) {
    	getActivity().runOnUiThread(() -> binding.containing.setText(containing));
    }
    
    private void setLastModified() {
        if(items.length > 1) {
            hide(binding.lastModifiedText, binding.lastModified);
            return;
        }
        binding.lastModified.setText(
            Files.formatToDate(items[0].lastModified()));
    }

    private void setPath() {
        if(items.length > 1) {
            hide(binding.pathText, binding.path);
            return;
        }
        binding.path.setText(items[0].getPath());
    }
    
    private String getName() {
    	if(items.length <= 1) 
            return items[0].getName();
        
        String name = getString(R.string.item_format);
        return String.format(name, items.length);
    }
    
    private void hide(View p1, View p2) {
    	p1.setVisibility(View.GONE);
        p2.setVisibility(View.GONE);
    }
}
