package com.abiddarris.lanfileviewer.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.settings.Settings;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ConnectingDialog extends DialogFragment {
    
    public static final String NAME = "name";
  
    private AlertDialog dialog;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        ConnectViewModel viewModel = new ViewModelProvider(this)
            .get(ConnectViewModel.class);
        
        viewModel.init(this);
        
        dialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.connecting_dialog_title)
            .setMessage(viewModel.formatMessage())
            .setNeutralButton(R.string.cancel, (p1,p2) -> {
                dismiss();
                getActivity().finish();
            })
            .create();
        
        setCancelable(false);
        
        viewModel.startCountDown(dialog);
        
        return dialog;
    }
    
    public static class ConnectViewModel extends ViewModel {
        
        private AlertDialog dialog;
        private CountdownRunnable runnable;
        private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
        private int seconds;
        private String secondString;
        private String message;
        private String name;
        
        private void init(ConnectingDialog dialog) {
            if(this.dialog != null) {
                return;
            }
            Bundle argument = dialog.getArguments();
        
            name = argument.getString(NAME);
            message = dialog.getString(R.string.connecting_dialog_desc);
            seconds = Settings.getConnectTimeout(dialog.getContext());
            secondString = dialog.getString(R.string.second);
        }
        
        private String formatMessage() {
            return String.format(message, name, seconds, secondString);
        }
        
        private void startCountDown(AlertDialog dialog) {
            this.dialog = dialog;
            
            if(runnable != null) return;
            runnable = new CountdownRunnable();
        	handler.postDelayed(runnable, 1000);
        }
        
        private class CountdownRunnable extends BaseRunnable {
        
            private boolean cancel;
            
            @Override
            public void onExecute(BaseRunnable context) throws Exception {
                super.onExecute(context);
                
                if(cancel) return;
            
                seconds--;
                dialog.setMessage(formatMessage());
            
                if(seconds > 0 && !cancel)
                    handler.postDelayed(this, 1000);
            }
            
        }
        
        @Override
        protected void onCleared() {
            super.onCleared();
            
            dialog = null;
            if(runnable != null) {
                runnable.cancel = true;
            }
        }
        
        
    }
    
}