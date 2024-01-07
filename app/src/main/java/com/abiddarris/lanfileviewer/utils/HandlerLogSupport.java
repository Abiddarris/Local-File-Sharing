package com.abiddarris.lanfileviewer.utils;

import android.os.Handler;
import android.os.Message;

public class HandlerLogSupport {

    private Handler handler;

    public HandlerLogSupport(Handler handler) {
        this.handler = handler;
    }

    public boolean post(RunnableInterface runnable) {
        return handler.post(new BaseRunnable(runnable));
    }
    
    public Message obtainMessage() {
        return handler.obtainMessage();
    }
    
    public boolean sendMessage(Message message) {
    	return handler.sendMessage(message);
    }
}
