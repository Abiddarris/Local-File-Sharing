package com.abiddarris.lanfileviewer.utils;

public class Timer {
    
    private long time = System.currentTimeMillis();
    
    public long reset() {
    	long newTime = System.currentTimeMillis();
        long timeTook = newTime - time;
        
        time = newTime;
        return timeTook;
    }
    
}
