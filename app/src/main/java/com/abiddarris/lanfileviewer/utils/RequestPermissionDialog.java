package com.abiddarris.lanfileviewer.utils;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.lanfileviewer.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RequestPermissionDialog extends DialogFragment {

    @Override
    @MainThread
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        return new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.permission)
                .setMessage(R.string.request_permission_message)
                .setCancelable(false)
                .setNeutralButton(R.string.exit, (p1,p2) -> getActivity().finishAffinity())
                .setPositiveButton(R.string.grant, (p1, p2) -> Permission.requestPermissions())
                .create();
    }
}
