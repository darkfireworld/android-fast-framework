package com.darkgem.framework.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.darkgem.framework.io.event.ScreenEvent;
import de.greenrobot.event.EventBus;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            Log.d(ScreenReceiver.class.getName(), "screen is on...");
            EventBus.getDefault().post(new ScreenEvent(false));
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            Log.d(ScreenReceiver.class.getName(), "screen is off...");
            EventBus.getDefault().post(new ScreenEvent(true));
        }
    }
}
