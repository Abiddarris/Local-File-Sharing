package com.abiddarris.lanfileviewer.actions;

import android.view.View;
import com.abiddarris.lanfileviewer.databinding.DialogActionProgressBinding;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;

public abstract class ActionRunnable extends BaseRunnable {

    private ActionDialog dialog;

    protected void attachDialog(ActionDialog dialog) {
    	this.dialog = dialog;
    }
    
    public String getTitle() {
        return "";
    }

    public ActionDialog getDialog() {
        return this.dialog;
    }
    
    public DialogActionProgressBinding getView() {
    	return getDialog().getViewBinding();
    }

}
