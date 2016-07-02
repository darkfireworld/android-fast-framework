package com.darkgem.framework.service;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import com.darkgem.framework.io.event.DownloadEvent;
import de.greenrobot.event.EventBus;

/**
 * 下载监听器
 */
public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            DownloadManager.Query query = new DownloadManager.Query();
            //在广播中取出下载任务的id
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            query.setFilterById(id);
            Cursor cursor = manager.query(query);
            try {
                if (cursor.moveToFirst()) {
                    //获取文件下载路径
                    String filename = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    //如果文件名不为空，说明已经存在了，拿到文件名想干嘛都好
                    if (filename != null) {
                        EventBus.getDefault().post(new DownloadEvent(id, filename));
                    }
                }
            } catch (Exception e) {
                Log.e(DownloadReceiver.class.getName(), e.getMessage(), e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }
}
