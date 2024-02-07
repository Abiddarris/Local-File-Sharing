package com.abiddarris.lanfileviewer.utils;

public class BasePoolable implements Poolable {

    private boolean freed = true;
    
    @Override
    public void onFreed() {
        freed = true;
    }

    @Override
    public boolean isFree() {
        return freed;
    }

    @Override
    public void onPooled() {
        freed = false;
    }

    @Override
    public void checkNotFreed() {
        if(freed) {
            throw new FreedException("This object is in the pool!");
        }
    }
    
}