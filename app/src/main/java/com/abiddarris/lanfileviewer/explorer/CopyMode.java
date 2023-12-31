package com.abiddarris.lanfileviewer.explorer;

import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.runnables.CopyRunnable;
import com.abiddarris.lanfileviewer.databinding.LayoutCopyMoveBinding;
import com.abiddarris.lanfileviewer.file.File;
import java.util.HashSet;
import java.util.Set;

public class CopyMode extends BottomToolbarMode {

    private Set<File> items;

    public CopyMode(Explorer explorer) {
        super(explorer);
    }
    
    @Override
    public void onModeDeselected() {
        super.onModeDeselected();
        
        items = null;
    }

    @Override
    public void onModifyOptionsCreated(RelativeLayout group) {
        LayoutInflater inflater = LayoutInflater.from(getExplorer().getContext());
        LayoutCopyMoveBinding binding = LayoutCopyMoveBinding.inflate(inflater);
        
        String totalItemsText = String.format(
            getExplorer().getContext().getString(R.string.item_format), items.size());
        if(items.size() > 1) totalItemsText += "s";
      
        binding.totalItems.setText(totalItemsText);
        binding.cancel.setOnClickListener((v) -> dismissMode());
        binding.moveToHere.setOnClickListener((v) -> {
            CopyRunnable runnable = new CopyRunnable(getExplorer().getParent(), items); 
            new ActionDialog(getExplorer(), runnable)
                .show(getExplorer().getFragment().getParentFragmentManager(), null);
                
            dismissMode();
        });
        
        group.addView(binding.getRoot());
    }
    
    private void dismissMode() {
        getExplorer().setMode(getExplorer().navigateMode);
    }

    public Set<File> getItems() {
        return this.items;
    }

    public void setItems(Set<File> items) {
        this.items = items;
    }
}
