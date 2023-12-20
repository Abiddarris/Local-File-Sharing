package com.abiddarris.lanfileviewer.actions;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import com.abiddarris.lanfileviewer.databinding.DialogActionProgressBinding;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.R;

public abstract class ActionRunnable extends BaseRunnable {

    private ActionDialog dialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    
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

    protected void updateFileInfo(String name, int index, int totalFiles) {
        handler.post(() -> {
            getView().name.setText(name);
            getView().progress.setText((index) + "/" + totalFiles);
        });
    }
    
    protected void setMaxProgress(double maxProgress) {
        handler.post(() -> getView().progressIndicator.setMax((int) maxProgress));
    }

    protected void updateProgress(double progress) {
        int max = getView().progressIndicator.getMax();
        double oldProgress = getView().progressIndicator.getProgress();
        double oldPercentage = Math.floor(oldProgress / max * 100);
        
        int percentage = (int)Math.floor(progress / max * 100);
        if(percentage > oldPercentage) {
            handler.post(() -> {
                getView().progressIndicator.setProgress((int) progress);
                getView().progressPercent.setText(percentage + "%");
            });
        }
    }

    protected void start() {
        handler.post(() -> {
            getView().name.setVisibility(View.VISIBLE);
            getView().progressPercent.setVisibility(View.VISIBLE);
        });
    }

    protected void prepare() {
        handler.post(() -> {
            getView().name.setVisibility(View.INVISIBLE);
            getView().progressPercent.setVisibility(View.INVISIBLE);

            getView().progress.setText(R.string.preparing);
        });
    }
    
}
