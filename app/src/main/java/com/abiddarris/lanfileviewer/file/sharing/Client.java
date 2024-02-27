package com.abiddarris.lanfileviewer.file.sharing;

public class Client {
    private String clientId;
    private String client;
    private long clientTimeout;
    
    public Client(String clientId, String client, long clientTimeout) {
        this.clientId = clientId;
        this.client = client;
        this.clientTimeout = clientTimeout;
    }

    public String getClientId() {
        return this.clientId;
    }
    
    public String getClient() {
        return this.client;
    }
    
    public long getClientTimeout() {
        return this.clientTimeout;
    }
    
}