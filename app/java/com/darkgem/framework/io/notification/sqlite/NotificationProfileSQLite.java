package com.darkgem.framework.io.notification.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.darkgem.framework.io.notification.model.NotificationProfile;
import com.darkgem.framework.io.support.AbsSQLite;
import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created by Administrator on 2016/1/13.
 */
public class NotificationProfileSQLite extends AbsSQLite {
    //SQl 字段
    static class SQL {
        //表名
        @NonNull
        final static String TABLE = "TABLE_NOTIFICATION_PROFILE";
        //ID -> 固定为0
        @NonNull
        final static String ID = "ID";
        //是否进行提示
        @NonNull
        final static String ALERT = "ALERT";
        //是否显示详情
        @NonNull
        final static String DETAIL = "DETAIL";
        //是否发出声音
        @NonNull
        final static String VOICE = "VOICE";
        //是否震动
        @NonNull
        final static String SHAKE = "SHAKE";
    }

    //默认ID为0
    static final int ID = 0;

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s (" +
                        "%s INTEGER NOT NULL ," +
                        "%s INTEGER NOT NULL ," +
                        "%s INTEGER NOT NULL ," +
                        "%s INTEGER NOT NULL ," +
                        "%s INTEGER NOT NULL ," +
                        "PRIMARY KEY(%s)" +
                        ")",
                SQL.TABLE,
                SQL.ID, SQL.ALERT, SQL.DETAIL, SQL.VOICE, SQL.SHAKE,
                SQL.ID);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Nullable
    public NotificationProfile select() {
        NotificationProfile ret = null;
        Cursor cursor = null;
        try {
            String sql = String.format("SELECT %s ,%s ,%s ,%s ,%s FROM %s WHERE %s = ?",
                    SQL.ID, SQL.ALERT, SQL.DETAIL, SQL.VOICE, SQL.SHAKE,
                    SQL.TABLE,
                    SQL.ID);
            cursor = getDb().rawQuery(sql, new String[]{String.valueOf(ID)});
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                boolean alert = cursor.getInt(1) == 1;
                boolean detail = cursor.getInt(2) == 1;
                boolean voice = cursor.getInt(3) == 1;
                boolean shake = cursor.getInt(4) == 1;
                ret = new NotificationProfile(alert, detail, voice, shake);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return ret;
    }

    public void merge(NotificationProfile notificationProfile) {
        getDb().beginTransaction();
        try {
            NotificationProfile o = select();
            if (o == null) {
                //插入
                String sql = String.format("INSERT INTO %s (%s ,%s ,%s ,%s, %s) VALUES (? ,? ,? ,? ,?)",
                        SQL.TABLE,
                        SQL.ID, SQL.ALERT, SQL.DETAIL, SQL.VOICE, SQL.SHAKE);
                getDb().execSQL(sql, new Object[]{ID, notificationProfile.isAlert() ? 1 : 0, notificationProfile.isDetail() ? 1 : 0, notificationProfile.isVoice() ? 1 : 0, notificationProfile.isShake() ? 1 : 0});
            } else {
                //更新
                String sql = String.format("UPDATE %s SET %s=? ,%s=?, %s=? ,%s=? WHERE %s = ?",
                        SQL.TABLE,
                        SQL.ALERT, SQL.DETAIL, SQL.VOICE, SQL.SHAKE,
                        SQL.ID);
                getDb().execSQL(sql, new Object[]{notificationProfile.isAlert() ? 1 : 0, notificationProfile.isDetail() ? 1 : 0, notificationProfile.isVoice() ? 1 : 0, notificationProfile.isShake() ? 1 : 0, ID});
            }
            getDb().setTransactionSuccessful();
        } finally {
            getDb().endTransaction();
        }
    }
}
