package com.abiddarris.lanfileviewer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.gretta.util.log.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Settings {
    
    public static final String TAG = Log.getTag(Settings.class);
    
    public static String getDefaultName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("name", Build.BRAND + " " + Build.DEVICE);
    }
    
    public static List<File> getRoots(Context context) {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> rootPaths = preferences.getStringSet("roots", createDefaultRoots(context));
        List<File> roots = new ArrayList<>();
        int index = 0;
        for(String path : rootPaths) {
            roots.add(new File(path));
        	index++;
        }
        
        Log.debug.log(TAG, "root paths : " + rootPaths);
        Log.debug.log(TAG, "roots : " + roots);
        
        return roots;
    }
    
    private static Set<String> createDefaultRoots(Context context) {
    	Set<String> roots = new HashSet<>();
        roots.add(LocalFileSource.getInternalStoragePath());
       
        if(LocalFileSource.hasSDCard(context)) {
            roots.add(LocalFileSource.getExternalStoragePath(context));
        }
        
        return roots;
    }
    
}
