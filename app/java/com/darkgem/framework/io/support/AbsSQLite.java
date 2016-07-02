package com.darkgem.framework.io.support;

import android.content.Context;
import android.support.annotation.Nullable;
import net.sqlcipher.database.SQLiteDatabase;

abstract public class AbsSQLite {
    SQLiteDatabase db;
    Context context;

    protected SQLiteDatabase getDb() {
        return db;
    }


    protected Context getContext() {
        return context;
    }

    /**
     * 数据库打开的时候, 调用该函数
     */
    void onOpen(SQLiteDatabase db) {
    }

    /**
     * 不存在数据库的时候调用该接口
     */
    public abstract void onCreate(SQLiteDatabase db);

    /**
     * 数据库版本变化的时候, 调用该接口升级
     */
    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    /**
     * 初始化该对象
     */
    public void init(Context context, SQLiteDatabase db) {
        this.context = context;
        this.db = db;
    }

    /**
     * 获取占位符?
     *
     * @param size 多少个?
     * @return 如果没有，则返回null
     */
    @Nullable
    protected String getPlaceholder(int size) {
        StringBuilder sb = null;
        for (int i = 0; i < size; ++i) {
            if (sb == null) {
                sb = new StringBuilder();
                sb.append("?");
            } else {
                sb.append(",");
                sb.append("?");
            }
        }
        //如果没有，则返回空字符串
        if (sb == null) {
            return null;
        } else {
            return sb.toString();
        }
    }
}
