package com.abiddarris.lanfileviewer.ui;

import androidx.annotation.NonNull;
import android.os.Bundle;
import android.app.Dialog;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogExceptionBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class ExceptionDialog extends DialogFragment {

    private DialogExceptionBinding binding;
    private String errorMessage;

    public ExceptionDialog(Exception e) {
        CharArrayWriter caw = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(caw);
        e.printStackTrace(writer);
        
        writer.close();
        
        errorMessage = caw.toString();
    }
    
    @Override
    @MainThread
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        binding = DialogExceptionBinding.inflate(getLayoutInflater());
        binding.exceptionText.setText(errorMessage);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setPositiveButton(R.string.ok, (p1,p2) -> {})
            .setTitle(R.string.exception_dialog_title)
            .setView(binding.getRoot())
            .create();
        
        return dialog;
    }
    
}
