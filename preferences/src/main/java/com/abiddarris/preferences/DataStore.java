package com.abiddarris.preferences;

public interface DataStore {
    
    String getString(String key);
    
    boolean getBoolean(String key);
    
    void store(String key, String value);
    
    void store(String key, boolean value);
    
}
