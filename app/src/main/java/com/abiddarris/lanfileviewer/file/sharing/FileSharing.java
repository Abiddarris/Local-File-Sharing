package com.abiddarris.lanfileviewer.file.sharing;

import android.content.Context;
import com.abiddarris.lanfileviewer.file.FileSource;

public class FileSharing {
    
    protected static final String SERVICE_TYPE = "_http._tcp.";
    
    public static SharingSession share(Context context, FileSource source) {
        return new SharingSession(context, source);
    }
    
    public static ScanningSession scan(Context context, ScanningSession.Callback callback) {
    	return new ScanningSession(context, callback);
    }
    
}
