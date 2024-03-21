package com.abiddarris.lanfileviewer.file.sharing;

import android.content.Context;

public class ConnectProperties {

    private Context context;
    private int timeout;
    private String ID;
    private String name;
    private String password;
    private OnConnectedListener onConnectedListener;
    private OnConnectionFailedListener onConnectionFailedListener;

    public ConnectProperties(Context context, String ID, String name) {
        this.context = context;
        this.ID = ID;
        this.name = name;
    }

    public ConnectProperties setPassword(String password) {
        this.password = password;

        return this;
    }

    public ConnectProperties setTimeout(int timeout) {
        this.timeout = timeout;

        return this;
    }
    
    public ConnectProperties setOnConnectionFailedListener(
            OnConnectionFailedListener onConnectionFailedListener) {
        this.onConnectionFailedListener = onConnectionFailedListener;
        
        return this;
    }
    
    public ConnectProperties setOnConnectedListener(OnConnectedListener onConnectedListener) {
        this.onConnectedListener = onConnectedListener;
        
        return this;
    }

    public Context getContext() {
        return this.context;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public String getID() {
        return this.ID;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public OnConnectedListener getOnConnectedListener() {
        return this.onConnectedListener;
    }
    
    public OnConnectionFailedListener getOnConnectionFailedListener() {
        return this.onConnectionFailedListener;
    }
    
    public static interface OnConnectedListener {
        void onConnected(NetworkFileSource source);
    }

    public static interface OnConnectionFailedListener {
        void onConnectionFailed(Exception e);
    }

}
