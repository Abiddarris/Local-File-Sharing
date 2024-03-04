package com.abiddarris.preferences;

public interface DataStore {
    
    String getString(String key);
    
    void store(String key, String value);
    
}
