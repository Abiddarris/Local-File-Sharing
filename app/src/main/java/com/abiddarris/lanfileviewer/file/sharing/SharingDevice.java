package com.abiddarris.lanfileviewer.file.sharing;

import static com.abiddarris.lanfileviewer.file.sharing.JSONRequest.*;

import android.net.nsd.NsdServiceInfo;

import com.abiddarris.lanfileviewer.file.sharing.ConnectProperties.OnConnectedListener;
import com.abiddarris.lanfileviewer.file.sharing.ConnectProperties.OnConnectionFailedListener;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.TaskResult;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SharingDevice {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    
    private InetAddress host;
    private int port;
    private String name;

    SharingDevice(NsdServiceInfo info) {
        host = info.getHost();
        port = info.getPort();
        name = info.getServiceName();
        name = name = name.substring(0, name.length() - "_FILEV".length());
    }
    
    public CancellationSignal openConnection(ConnectProperties properties) {
        Future<?> future = executor.submit(
            new BaseRunnable(c -> {
                try {
                    openConnectionInternal(properties);
                } catch (Exception e) {
                    OnConnectionFailedListener listener = properties.getOnConnectionFailedListener();
                    if(listener != null)    
                        listener.onConnectionFailed(e);
                }
            }));
        return new CancellationSignal(future);
    }
    
    private void openConnectionInternal(ConnectProperties properties) throws Exception {
        URL server = new URL(getBaseURL() + "/connect");
        
        JSONObject request = new JSONObject()
            .put(KEY_REQUEST, JSONRequest.createRequest(REQUEST_CONNECT))
            .put(KEY_CLIENT_ID, properties.getID())
            .put(KEY_CLIENT_NAME, properties.getName())
            .put(KEY_TIMEOUT, properties.getTimeout());
        
        String password = properties.getPassword();
        if(password != null) {
        	request.put(KEY_PASSWORD, password);
        }
        JSONObject response;
        try {
            response = NetworkFileSource.sendRequest(server, request, 5000, properties.getTimeout());
        } catch (RequestException e) {
            Throwable cause = e.getCause();
            if(cause != null && cause.getClass() == SocketTimeoutException.class) {
                throw new TimeoutException(e);
            }
            throw e;
        }
        
        if(Thread.currentThread().isInterrupted()) {
            throw new CancelledException();
        }
        
        int code = response.getInt(KEY_RESULT);
        if(code == RESULT_REJECTED) {
            throw new AccessRejectedException();
        } else if(code == RESULT_UNAUTHORIZED) {
            throw new UnauthorizedException();
        }
        
        OnConnectedListener listener = properties.getOnConnectedListener();
        if(listener != null)
            listener.onConnected(new NetworkFileSource(this, properties.getContext(), response));
    }
    
    String getBaseURL() {
        return "http://" + getHost().getHostName() +
            ":" + getPort();
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
    
    public static class CancellationSignal extends TaskResult {
        
        private Future<?> future;
        
        @SuppressWarnings("unchecked")
        CancellationSignal(Future<?> future) {
            super(future);
            
            this.future = future;
        }
        
        public void cancel() {
            future.cancel(true);
        }
        
    }
}
