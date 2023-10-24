package com.abiddarris.lanfileviewer.explorer;

import android.view.View;
import android.widget.CheckBox;
import com.abiddarris.lanfileviewer.explorer.FileAdapter.ViewHolder;

public class NavigateMode extends Mode {
    
    public NavigateMode(FileExplorer explorer) {
        super(explorer);
    }
    
    @Override
    public void onItemClickListener(ViewHolder holder, int pos) {
        getExplorer().open(getExplorer()
            .getAdapter().get(pos));
    }
    
    @Override
    public void onItemLongClickListener(ViewHolder holder, int pos) {
        getExplorer().selectMode.select(pos);
        getExplorer().setMode(getExplorer().selectMode);
    }
    
    @Override
    public void onViewBind(ViewHolder holder, int pos) {
        CheckBox checkBox = holder.binding.checkBox;
        checkBox.setVisibility(View.GONE);
    }
    
    @Override
    public boolean onBackPressed() {
        return getExplorer().navigateUp();
    }
    
}
