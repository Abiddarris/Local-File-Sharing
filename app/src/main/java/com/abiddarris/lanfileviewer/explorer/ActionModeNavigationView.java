package com.abiddarris.lanfileviewer.explorer;
import android.view.MenuItem;
import android.view.Menu;
import androidx.appcompat.view.ActionMode;

public class ActionModeNavigationView extends TopNavigationView implements ActionMode.Callback {

    private ActionMode mode;
    private Callback callback;
    
    public ActionModeNavigationView(Callback callback) {
        this.callback = callback;
    }
    
    @Override
    public void setCustomView(View view) {
        mode.setCustomView(view);
    }
    
    @Override
    public void finish() {
        mode.finish();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.mode = mode;
        callback.onShow(this);
        return true;
    }
    

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }
    

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menu) {
        return false;
    }
    

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        callback.onHide(this);
    }
    
}
