package com.abiddarris.lanfileviewer.utils;

import com.gretta.util.log.Log;

public class BaseRunnable implements Runnable, RunnableInterface {

    private RunnableInterface runnable;

    public BaseRunnable(RunnableInterface runnable) {
        this.runnable = runnable;
    }

    public BaseRunnable() {}

    @Override
    public void run() {
        try {
            onExecute(this);
        } catch (Exception e) {
            onError(e);
        } finally {
            onFinalization();
        }
    }

    public void onExecute(BaseRunnable context) throws Exception {
        if(this.runnable != null) this.runnable.onExecute(context);
    }

    public void onError(Exception e) {
        Log.err.log(getTag(), e);
    }

    public void onFinalization() {}

    public String getTag() {
        return Log.getTag(this.getClass());
    }
}
