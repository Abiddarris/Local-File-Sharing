package com.abiddarris.lanfileviewer.utils;

import android.os.Handler;

public class HandlerLogSupport {

    private Handler handler;

    public HandlerLogSupport(Handler handler) {
        this.handler = handler;
    }

    public void post(RunnableInterface runnable) {
        handler.post(new BaseRunnable(runnable));
    }
}
