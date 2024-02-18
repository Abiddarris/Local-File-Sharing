package com.abiddarris.lanfileviewer.file.sharing;

public interface ConnectListener {
    
    boolean accept(String clientId, String clientName);
    
}
