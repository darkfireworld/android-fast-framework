package com.darkgem.framework.io.app.profile.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.darkgem.framework.io.app.profile.model.ApplicationProfile;
import com.darkgem.framework.io.support.AbsSQLite;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 系统数据库
 */
public class ApplicationProfileSQLite extends AbsSQLite {
    // SQL 字段
    static class SQL {
        //表名
        @NonNull
        final static String TABLE = "TABLE_APPLICATION_PROFILE";
        //ID 指定为 0
        @NonNull
        final static String ID = "ID";
        //最后一次登入的用户ID
        @Nullable
        final static String USR_ID = "USR_ID";
        //主要使用的TOKEN
        @Nullable
        final static String MAIN_TOKEN = "MAIN_TOKEN";
        //APP 版本号
        @NonNull
        final static String VERSION = "VERSION";
    }

    //默认ID
    final static int ID = 0;

    //缓存系统信息
    ApplicationProfile cache = null;
    ReentrantLock lock = new ReentrantLock();


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s ( " +
                        "   %s INTEGER NOT NULL ," +
                        "   %s TEXT NULL        ," +
                        "   %s TEXT NULL        ," +
                        "   %s INTEGER NOT NULL ," +
                        "   PRIMARY KEY(%s)      " +
                        "   )",
                SQL.TABLE,
                SQL.ID, SQL.USR_ID, SQL.MAIN_TOKEN, SQL.VERSION,
                SQL.ID);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 获得系统信息
     */
    @Nullable
    public ApplicationProfile select() {
        if (cache != null) {
            return cache;
        }
        Cursor cursor = null;
        lock.lock();
        try {
            String sql = String.format("SELECT %s,%s,%s,%s FROM %s WHERE %s = ?",
                    SQL.ID, SQL.USR_ID, SQL.MAIN_TOKEN, SQL.VERSION,
                    SQL.TABLE,
                    SQL.ID);
            cursor = getDb().rawQuery(sql, new String[]{String.valueOf(ID)});
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String usrId = cursor.getString(1);
                String mainToken = cursor.getString(2);
                int version = cursor.getInt(3);
                //更新缓存
                cache = new ApplicationProfile(usrId, mainToken, version);
            }
        } finally {
            lock.unlock();
            if (cursor != null)
                cursor.close();
        }
        return cache;
    }

    /**
     * 更新/插入
     */
    public void merge(ApplicationProfile applicationProfile) {
        lock.lock();
        getDb().beginTransaction();
        try {
            if (select() == null) {
                //插入
                String sql = String.format("INSERT INTO %s (%s ,%s ,%s ,%s) VALUES(? ,? ,? ,? )",
                        SQL.TABLE,
                        SQL.ID, SQL.USR_ID, SQL.MAIN_TOKEN, SQL.VERSION);
                getDb().execSQL(sql, new Object[]{ID, applicationProfile.getUsrId(), applicationProfile.getMainToken(), applicationProfile.getVersion()});
            } else {
                String sql = String.format("UPDATE %s SET %s=? ,%s=? ,%s=? WHERE %s=? ",
                        SQL.TABLE,
                        SQL.USR_ID, SQL.MAIN_TOKEN, SQL.VERSION,
                        SQL.ID);
                //更新
                getDb().execSQL(sql, new Object[]{applicationProfile.getUsrId(), applicationProfile.getMainToken(), applicationProfile.getVersion(), ID});
            }
            //更新缓存
            cache = applicationProfile;
            getDb().setTransactionSuccessful();
        } finally {
            lock.unlock();
            getDb().endTransaction();
        }
    }
}
