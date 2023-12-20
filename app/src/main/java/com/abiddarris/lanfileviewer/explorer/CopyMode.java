package com.abiddarris.lanfileviewer.explorer;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.LayoutCopyMoveBinding;

public class CopyMode extends BottomToolbarMode {
    
    public CopyMode(Explorer explorer) {
        super(explorer);
    }
    
    @Override
    public void onModifyOptionsCreated(RelativeLayout group) {
        LayoutInflater inflater = LayoutInflater.from(getExplorer().getContext());
        LayoutCopyMoveBinding binding = LayoutCopyMoveBinding.inflate(inflater);
            
        group.addView(binding.getRoot());
    }
}
