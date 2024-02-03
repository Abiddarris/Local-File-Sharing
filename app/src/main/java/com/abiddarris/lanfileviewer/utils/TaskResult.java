package com.abiddarris.lanfileviewer.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskResult<T> {
    
    private Future<T> future;
    
    public TaskResult(Future<T> future) {
        this.future = future;
    }
    
    public T get() {
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new TaskException(e);
        }
    }
    
}
