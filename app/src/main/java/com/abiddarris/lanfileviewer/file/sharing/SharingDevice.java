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
    }
    
    public NetworkFileSource openConnection(Context context) throws Exception {
        return new NetworkFileSource(this, context);
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
}
