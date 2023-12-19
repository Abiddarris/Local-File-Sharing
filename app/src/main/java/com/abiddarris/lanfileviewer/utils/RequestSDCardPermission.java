package com.abiddarris.lanfileviewer.utils;
import android.os.Bundle;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RequestSDCardPermission extends DialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        return new MaterialAlertDialogBuilder(getContext())
            .setCancelable(false)
            .setTitle(R.string.permission)
            .setMessage(R.string.request_sd_card_permission_message)
            .setNeutralButton(R.string.exit, (p1,p2) -> getActivity().finishAffinity())
            .setPositiveButton(R.string.select, (p1, p2) -> Permission.requestSDCardWritePermission())
            .create();
    }
    
    
}
