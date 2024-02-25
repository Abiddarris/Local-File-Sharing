package com.abiddarris.lanfileviewer.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.settings.Settings;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ConnectingDialog extends DialogFragment {
    
    public static final String NAME = "name";
  
    private AlertDialog dialog;
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    private int seconds;
    private String secondString;
    private String message;
    private String name;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        Bundle argument = getArguments();
        
        name = argument.getString(NAME);
        message = getString(R.string.connecting_dialog_desc);
        seconds = Settings.getConnectTimeout(getContext());
        secondString = getString(R.string.second);
        
        dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.connecting_dialog_title)
            .setMessage(formatMessage())
            .create();
        
        handler.postDelayed(new CountdownRunnable(), 1000);
        
        return dialog;
    }
    
    private String formatMessage() {
        return String.format(message, name, seconds, secondString);
    }
    
    private class CountdownRunnable extends BaseRunnable {
        
        @Override
        public void onExecute(BaseRunnable context) throws Exception {
            super.onExecute(context);
            
            seconds--;
            dialog.setMessage(formatMessage());
            
            if(seconds >= 0)
                handler.postDelayed(this, 1000);
        }
        
    }
    
}