package com.darkgem.framework.io.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.darkgem.framework.io.app.ApplicationIo;
import com.darkgem.framework.io.notification.model.NotificationProfile;
import com.darkgem.framework.io.notification.sqlite.NotificationProfileSQLite;

import java.util.*;

/**
 * 通知栏管理
 */
public class NotificationIo {

    //通知ID管理类
    public static class IdManager {
        /**
         * 类型
         */
        private enum Type {
            ANONYMOUS_NOTICE;//匿名通知
        }

        //不同Type的Id保存Map
        Map<Type, Map<String, Integer>> idMaps = new HashMap<Type, Map<String, Integer>>();
        //全局唯一id保存
        Set<Integer> idSet = new HashSet<Integer>();
        //随机数生成
        Random random = new Random();

        //通过targetId 获取Id
        synchronized private Integer getId(Type type, String targetId) {
            Map<String, Integer> idMap = getIdMap(type);
            Integer id = idMap.get(targetId);
            //如果为非需要获取新Id, 则可以返回为空
            if (id != null)
                return id;
            //生成新的Id
            while (true) {
                //获取一个随机整数
                id = random.nextInt();
                if (!idSet.contains(id)) {
                    idSet.add(id);
                    idMap.put(targetId, id);
                    return id;
                }
            }
        }

        /**
         * 获得某个类型的IdMap
         */
        synchronized public Map<String, Integer> getIdMap(Type type) {
            Map<String, Integer> idMap = idMaps.get(type);
            if (idMap == null) {
                idMap = new HashMap<String, Integer>();
                idMaps.put(type, idMap);
            }
            return idMap;
        }

        /**
         * 获取所有的ID
         */
        synchronized List<Integer> getIds() {
            List<Integer> ret = new LinkedList<Integer>();
            for (Integer id : idSet) {
                ret.add(id);
            }
            return ret;
        }

        /**
         * 清空所有的IDS
         */
        synchronized void clearId() {
            idMaps.clear();
            idSet.clear();
        }
    }


    //匿名模式下，TARGET ID
    final static String ANONYMOUS_NOTICE_TARGET_ID = "ANONYMOUS_NOTICE_TARGET_ID";

    static volatile NotificationIo instance;
    Context context;
    NotificationManager notificationManager;
    IdManager idManager = new IdManager();
    //最后通知时间
    long lastNotificationTime = 0;


    private NotificationIo(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationIo getInstance(Context context) {
        if (instance == null) {
            synchronized (NotificationIo.class) {
                if (instance == null) {
                    instance = new NotificationIo(context);
                }
            }
        }
        return instance;
    }

    /**
     * 发送通知到通知栏
     */
    synchronized public void doNotification(IdManager.Type type, String targetId, String tickerText,
                                            Intent intent, String contentTitle, String contentText,
                                            boolean shake, boolean voice) {
        //读取主配置
        NotificationProfile notificationProfile = select();
        if (!notificationProfile.isAlert()) {
            //设置了免打扰, 则都关闭
            return;
        }
        if (!notificationProfile.isDetail()) {
            //通知类型为不显示详情-> 匿名
            type = IdManager.Type.ANONYMOUS_NOTICE;
            //修改TARGETID 为匿名ID
            targetId = ANONYMOUS_NOTICE_TARGET_ID;
            //修改所有文本都为匿名类型
            contentTitle = "消息";
            tickerText = contentText = "您有新的消息";
        }
        if (!notificationProfile.isVoice()) {
            voice = false;
        }
        if (!notificationProfile.isShake()) {
            shake = false;
        }
        //检测时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //在00:00->06:00 不发出声音
        if (calendar.get(Calendar.HOUR_OF_DAY) > 0 && calendar.get(Calendar.HOUR_OF_DAY) < 6) {
            voice = false;
        }
        //避免通知洪流
        long now = System.currentTimeMillis();
        if (now - lastNotificationTime < 1000 * 3) {
            //更改设置
            shake = false;
            voice = false;
        }
        lastNotificationTime = now;
        //默认为显示灯光
        int defaults = Notification.DEFAULT_LIGHTS;
        //设置通知表象
        if (voice) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (shake) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        Notification notification = new Notification();
        //TODO 添加icon
        //notification.icon = R.drawable.ic_launcher;
        notification.tickerText = tickerText;
        notification.when = System.currentTimeMillis();
        notification.defaults = defaults;
        //通知点击清除
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        //生成ID
        int id = idManager.getId(type, targetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setLatestEventInfo(context, contentTitle, contentText, pendingIntent);

        notificationManager.notify(id, notification);
    }

    /**
     * 清空所有的通知
     */
    synchronized public void clearNotification() {
        for (Integer id : idManager.getIds()) {
            notificationManager.cancel(id);
        }
        //清空所有的ID
        idManager.clearId();
    }

    /**
     * 获取当前通知模式
     */
    public NotificationProfile select() {
        NotificationProfile notificationProfile = ApplicationIo.getInstance(context).getPrivateSQLite(NotificationProfileSQLite.class).select();
        //如果没有查询到，则返回一个默认的
        if (notificationProfile == null) {
            return new NotificationProfile(true, true, true, true);
        }
        return notificationProfile;
    }

    /**
     * 更新通知模式
     */
    public void merge(NotificationProfile notificationProfile) {
        ApplicationIo.getInstance(context).getPrivateSQLite(NotificationProfileSQLite.class).merge(notificationProfile);
    }
}
