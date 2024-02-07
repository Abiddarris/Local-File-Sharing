package com.abiddarris.lanfileviewer.utils;

public interface Poolable {
    
    void onFreed();
    
    boolean isFree();
    
    void onPooled();
    
    void checkNotFreed();
    
}
