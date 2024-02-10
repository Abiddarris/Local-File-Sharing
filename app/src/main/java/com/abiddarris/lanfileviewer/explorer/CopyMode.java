package com.abiddarris.lanfileviewer.explorer;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.ActionRunnable;
import com.abiddarris.lanfileviewer.actions.runnables.CopyRunnable;
import com.abiddarris.lanfileviewer.databinding.LayoutCopyMoveBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.Files;
import java.util.HashSet;
import java.util.Set;

public class CopyMode extends BottomToolbarMode {

    private File[] items;
    private LayoutCopyMoveBinding binding;

    public CopyMode(Explorer explorer) {
        super(explorer);
    }
    
    @Override
    public void onModeDeselected() {
        super.onModeDeselected();
        
        items = null;
    }

    @Override
    public void onBottomToolbarShown(ViewGroup group) {
        LayoutInflater inflater = LayoutInflater.from(getExplorer().getContext());
        binding = LayoutCopyMoveBinding.inflate(inflater);
        
        String totalItemsText = Files.formatFromItems(
            getExplorer().getContext(),items
        );
        
        binding.totalItems.setText(totalItemsText);
        binding.cancel.setOnClickListener((v) -> dismissMode());
        binding.action.setText(
            getExplorer().getContext().getString(getActionText()));
        binding.action.setOnClickListener((v) -> {
            ActionRunnable runnable = getRunnable(items, getExplorer().getParent());
            new ActionDialog(getExplorer(), runnable)
                .show(getExplorer().getFragment().getParentFragmentManager(), null);
                
            dismissMode();
        });
        
        group.addView(binding.getRoot());
    }
    
    @Override
    public void onParentChanged(File newParent) {
        for(File file : items) {
        	if(file.getPath().equals(newParent.getPath())) {
                if(binding.action.isEnabled()) {
                    binding.action.setEnabled(false);
                }
                return;
            }
        }
        if(!binding.action.isEnabled()) {
            binding.action.setEnabled(true);
        }
    }
    
    protected ActionRunnable getRunnable(File[] items, FilePointer dest) {
        return new CopyRunnable(dest, items); 
    }
    
    protected int getActionText() {
        return R.string.copy_to_here;
    }
    
    private void dismissMode() {
        getExplorer().setMode(getExplorer().navigateMode);
    }

    public void setItems(File[] items) {
        this.items = items;
    }
}
