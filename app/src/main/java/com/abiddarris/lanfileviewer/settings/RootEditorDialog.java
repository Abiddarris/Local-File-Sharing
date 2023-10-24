package com.abiddarris.lanfileviewer.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.DialogPreference;
import com.abiddarris.lanfileviewer.R;

public class RootEditorDialog extends DialogFragment {
    
    @Override
    @MainThread
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        return inflater.inflate(R.layout.dialog_root_editor , group, false);
    }
    
}
