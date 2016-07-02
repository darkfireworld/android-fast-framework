package com.darkgem.framework.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.darkgem.framework.io.event.NetworkEvent;
import com.darkgem.framework.support.kit.NetworkKit;
import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2015/4/9.
 */
public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean open = NetworkKit.isNetworkAvailable(context);
        if (open)
            EventBus.getDefault().post(NetworkEvent.CONNECTED);
        else
            EventBus.getDefault().post(NetworkEvent.DISCONNECT);
    }
}
