package com.darkgem.framework.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.darkgem.framework.io.app.ApplicationIo;

/**
 * Created by Administrator on 2015/10/7.
 */
public class KeepAliveService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //初始化
        ApplicationIo.getInstance(this).tryInitializeCore();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
