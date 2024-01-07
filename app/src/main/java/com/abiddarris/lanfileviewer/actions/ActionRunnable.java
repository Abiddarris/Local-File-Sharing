package com.abiddarris.lanfileviewer.actions;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import com.abiddarris.lanfileviewer.databinding.DialogActionProgressBinding;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.gretta.util.log.Log;

public abstract class ActionRunnable extends BaseRunnable implements Handler.Callback {

    private ActionDialog dialog;
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper(), this));
    
    private static final String KEY_NAME = "name";
    private static final String KEY_INDEX = "index";
    private static final String KEY_TOTAL_FILES = "totalFiles";
    private static final String KEY_MAX_PROGRESS = "maxProgress";
    private static final String KEY_PROGRESS = "progress";
    private static final String KEY_PERCENTAGE = "percentage";
    private static final int UPDATE_FILE_INFO = 0;
    private static final int UPDATE_MAX_PROGRESS = 1;
    private static final int UPDATE_PROGRESS = 2;
    
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
    
    @Override
    public boolean handleMessage(Message message) {
        switch(message.what) {
            case UPDATE_FILE_INFO :
                Bundle data = message.getData();
                String name = data.getString(KEY_NAME);
                int index = data.getInt(KEY_INDEX);
                int totalFiles = data.getInt(KEY_TOTAL_FILES);
            
                getView().name.setText(name);
                getView().progress.setText((index) + "/" + totalFiles);
            
                Log.debug.log(getTag(), "updating file info with var name : " + name + ", index : " + index + ", totalFiles" + totalFiles);
                break;
            case UPDATE_MAX_PROGRESS :
                data = message.getData();
                long maxProgress = data.getLong(KEY_MAX_PROGRESS);
                getView().progressIndicator.setMax((int) maxProgress);
                getView().progressIndicator.setProgress(0);
            
                Log.debug.log(getTag(), "setting max progress : " + maxProgress);
                break;
            case UPDATE_PROGRESS :
                data = message.getData();
                
                long progress = data.getLong(KEY_PROGRESS);
                int max = getView().progressIndicator.getMax();
                double oldProgress = getView().progressIndicator.getProgress();
                double oldPercentage = Math.floor(oldProgress / max * 100);
        
                int percentage = (int)Math.floor((double)progress / max * 100);
                Log.debug.log(getTag(), "try to update progress with max : " + max + ", oldProgress : " + oldProgress + ", old percentage : " + oldPercentage + ", percentage : " + percentage);
                if(percentage > oldPercentage) {
                    getView().progressIndicator.setProgress((int) progress);
                    getView().progressPercent.setText(percentage + "%");
            
                    Log.debug.log(getTag(), "updating progress progress : " + progress + ", percentage : " + percentage);
                }
        }
        return true;
    }
    

    protected void updateFileInfo(String name, int index, int totalFiles) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NAME, name);
        bundle.putInt(KEY_INDEX, index);
        bundle.putInt(KEY_TOTAL_FILES, totalFiles);
        
        Message message = handler.obtainMessage();
        message.what = UPDATE_FILE_INFO;
        message.setData(bundle);
        
        handler.sendMessage(message);
    }
    
    protected void setMaxProgress(long maxProgress) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_MAX_PROGRESS, maxProgress);
        
        Message message = handler.obtainMessage();
        message.what = UPDATE_MAX_PROGRESS;
        message.setData(bundle);
        
        handler.sendMessage(message);
    }

    protected void updateProgress(long progress) {
        Message message = handler.obtainMessage();
        
        Bundle bundle = message.getData();
        bundle.putLong(KEY_PROGRESS, progress);
        
        message.what = UPDATE_PROGRESS;
        handler.sendMessage(message);
    }

    protected void start() {
        handler.post((c) -> {
            getView().name.setVisibility(View.VISIBLE);
            getView().progressPercent.setVisibility(View.VISIBLE);
        });
    }

    protected void prepare() {
        handler.post((c) -> {
            getView().name.setVisibility(View.INVISIBLE);
            getView().progressPercent.setVisibility(View.INVISIBLE);
            getView().progress.setText(R.string.preparing);
        });
    }
    
}
