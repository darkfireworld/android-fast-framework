package com.darkgem.framework.io.event;

/**
 * Created by Administrator on 2015/5/28.
 */
public class ScreenEvent {
    boolean lock = false;

    public ScreenEvent(boolean lock) {
        this.lock = lock;
    }


    public boolean isLock() {
        return lock;
    }
}
