package com.abiddarris.lanfileviewer.settings;

import android.content.Context;
import android.util.AttributeSet;
import androidx.fragment.app.DialogFragment;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.R;

public class CustomPreference extends DialogPreference {
    
    public CustomPreference(Context context, AttributeSet attrs) {
        super(context,attrs);
        
        setPersistent(false);
    }
    
    static DialogFragment createFrom(final Preference pref) {
        if(pref.getKey().equals("roots")) {
            return new RootEditorDialog();
        } else if(pref.getKey().equals("thumbnailsCache")) {
            return new DeleteThumbnailsCacheDialog();
        } else if(pref.getKey().equals("downloadsCache")) {
            return new DeleteDownloadCacheDialog();
        }
        return null;
    }
    
}
