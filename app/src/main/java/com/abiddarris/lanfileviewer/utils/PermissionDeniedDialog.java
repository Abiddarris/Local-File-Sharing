package com.abiddarris.lanfileviewer.utils;

import android.app.Dialog;
import android.os.Bundle;
import com.abiddarris.lanfileviewer.R;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PermissionDeniedDialog extends DialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        return new MaterialAlertDialogBuilder(getContext())
            .setTitle(R.string.error)
            .setMessage(R.string.permission_denied_message)
            .setCancelable(false)
            .setPositiveButton(R.string.exit, (p1, p2) -> getActivity().finishAffinity())
            .create();
    }
    
    
}
