package com.abiddarris.lanfileviewer.file.sharing;

import android.content.Context;

public class FileSharing {
    
    protected static final String SERVICE_TYPE = "_http._tcp.";
    
    public static SharingSession share(Context context) {
        return new SharingSession(context);
    }
    
    public static ScanningSession scan(Context context, ScanningSession.Callback callback) {
    	return new ScanningSession(context, callback);
    }
    
}
