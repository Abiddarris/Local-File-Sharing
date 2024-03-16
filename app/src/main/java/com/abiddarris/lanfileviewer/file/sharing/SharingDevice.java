package com.abiddarris.lanfileviewer.file.sharing;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import java.net.InetAddress;

public class SharingDevice {

    private InetAddress host;
    private int port;
    private String name;

    SharingDevice(NsdServiceInfo info) {
        host = info.getHost();
        port = info.getPort();
        name = info.getServiceName();
        name = name = name.substring(0, name.length() - "_FILEV".length());
    }
    
    public NetworkFileSource openConnection(Context context, int timeout) throws Exception {
        return openConnection(context, null, timeout);
    }
    
    public NetworkFileSource openConnection(Context context, String password, int timeout) throws Exception {
        return new NetworkFileSource(this, context, password, timeout);
    }

    public InetAddress getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getName() {
    	return name;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SharingDevice)) return false;
        if(obj == this) return true;
        
        SharingDevice device = (SharingDevice)obj;
        
        return (name == null ? device.name == null : name.equals(device.name)) &&
            (host == null ? device.host == null : host.equals(device.host)) &&
            (port == device.port);
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = hash * 11 + (name == null ? 0 : name.hashCode());
        hash = hash * 11 + (host == null ? 0 : host.hashCode());
        hash = hash * 11 + port;
        
        return hash;
    }
    
}
