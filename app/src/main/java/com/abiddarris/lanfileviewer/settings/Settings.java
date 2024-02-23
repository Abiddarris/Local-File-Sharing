package com.abiddarris.lanfileviewer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.preference.PreferenceManager;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.bumptech.glide.Glide;
import com.gretta.util.RandomString;
import com.gretta.util.Randoms;
import com.gretta.util.log.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Settings {
    
    public static final String TAG = Log.getTag(Settings.class);
    
    private static final String ID = "id";
    private static final String CONFIRM_CONNECT_REQUEST = "confirmConnectRequest";
    private static final String PASSWORD = "password";
    
    public static String getDefaultName(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("name", Build.BRAND + " " + Build.DEVICE);
    }
    
    public static List<File> getRoots(Context context) {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> rootPaths = preferences.getStringSet("roots", createDefaultRoots(context));
        List<File> roots = toList(rootPaths);
        
        Log.debug.log(TAG, "root paths : " + rootPaths);
        Log.debug.log(TAG, "roots : " + roots);
        
        return roots;
    }
    
    public static void setRoots(Context context, List<File> files) {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> paths = new HashSet<>();
        for(File file : files) {
        	paths.add(file.getAbsolutePath());
        }
        
        preferences.edit()
            .putStringSet("roots", paths)
            .commit();
    }
    
    public static List<File> getDefaultRoots(Context context) {
    	return toList(createDefaultRoots(context));
    }
    
    public static String getId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String id = preferences.getString(ID, null);
        
        if(id == null) {
            id = Randoms.getRandomString()
                .get(16);
            
            preferences.edit()
                .putString(ID, id)
                .commit();
        }
        
        return id;
    }
    
    public static boolean isConfirmConnectRequest(Context context) {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(CONFIRM_CONNECT_REQUEST, false);
    }
    
    public static String getPassword(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String password = preferences.getString(PASSWORD, "");
        return password == null ? null : (password.equals("") ? null : password);
    }
    
    private static List<File> toList(Set<String> paths) {
        List<File> roots = new ArrayList<>();
        int index = 0;
        for(String path : paths) {
            roots.add(new File(path));
        	index++;
        }
        
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
