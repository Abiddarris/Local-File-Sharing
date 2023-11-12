package com.abiddarris.lanfileviewer.file.sharing;

import android.content.Context;

public class FileSharing {
    
    protected static final String SERVICE_TYPE = "_http._tcp.";
    
    public static SharingSession share(Context context) {
        return new SharingSession(context);
    }
    
}
