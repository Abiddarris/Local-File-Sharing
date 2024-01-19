package com.abiddarris.lanfileviewer.settings;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.R;

public class RootEditorPreference extends DialogPreference {
    
    public RootEditorPreference(Context context, AttributeSet attrs) {
        super(context,attrs);
        
        setPersistent(false);
    }
    
}
