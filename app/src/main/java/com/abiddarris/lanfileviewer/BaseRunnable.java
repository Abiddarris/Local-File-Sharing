package com.abiddarris.lanfileviewer;

import com.gretta.util.log.Log;

public abstract class BaseRunnable implements Runnable {
    
    @Override
    public void run() {
        try {
            onExecute();
        } catch (Exception e) {
            onError(e);
        } finally {
            onFinalization();
        }
    }
    
    public abstract void onExecute() throws Exception;
    
    public void onError(Exception e) {
    	Log.err.log(getTag(), e);
    }
    
    public void onFinalization() {
    }
    
    public String getTag() {
    	return Log.getTag(this.getClass());
    }
}
