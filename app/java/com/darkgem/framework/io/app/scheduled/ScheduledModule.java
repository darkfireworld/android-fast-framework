package com.darkgem.framework.io.app.scheduled;

import android.content.Context;
import com.darkgem.framework.support.kit.ThreadPoolKit;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/1/13.
 */
public class ScheduledModule {
    /**
     * 周期性缓冲池
     */
    ScheduledExecutorService scheduledThreadPool;
    Context context;

    public ScheduledModule(Context context) {
        this.context = context;
        this.scheduledThreadPool = ThreadPoolKit.createScheduledExecutorService(1, "app-scheduled");
    }

    /**
     * 调度API
     */
    public void schedule(Runnable runnable, long delay, TimeUnit unit) {
        scheduledThreadPool.schedule(runnable, delay, unit);
    }
}
