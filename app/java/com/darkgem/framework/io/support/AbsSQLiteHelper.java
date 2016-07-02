package com.darkgem.framework.io.support;

import android.content.Context;
import android.util.Log;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbsSQLiteHelper extends SQLiteOpenHelper {
    Map<Class, AbsSQLite> map;
    Context context;
    String name;
    String key;

    public AbsSQLiteHelper(Context context, String name, int version, String key) {
        super(context, name, null, version);
        this.context = context;
        this.name = name;
        this.key = key;
        this.map = new ConcurrentHashMap<Class, AbsSQLite>();
        //生存对象
        List<Class<? extends AbsSQLite>> list = registerAbsDao();
        for (Class cls : list) {
            try {
                if (map.get(cls) != null) {
                    throw new RuntimeException("存在相同的类进行注册: " + cls.getName());
                }
                map.put(cls, (AbsSQLite) cls.newInstance());
            } catch (Exception e) {
                Log.e(AbsSQLiteHelper.class.getName(), e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        //初始化各个SQLite
        SQLiteDatabase db = getWritableDatabase();
        for (Map.Entry<Class, AbsSQLite> entry : map.entrySet()) {
            entry.getValue().init(context, db);
        }
    }


    /**
     * 默认输入密码的数据库
     */
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase(key);
    }

    /**
     * 注册将要被使用的Dao 列表
     */
    protected abstract <T extends AbsSQLite> List<Class<? extends AbsSQLite>> registerAbsDao();

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        //给予各个sqlite onOpen的机会
        for (Map.Entry<Class, AbsSQLite> entry : map.entrySet()) {
            entry.getValue().onOpen(db);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Map.Entry<Class, AbsSQLite> entry : map.entrySet()) {
            entry.getValue().onCreate(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Map.Entry<Class, AbsSQLite> entry : map.entrySet()) {
            entry.getValue().onUpgrade(db, oldVersion, newVersion);
        }
    }

    /**
     * Dao获取
     */
    public <T extends AbsSQLite> T getSQLite(Class<T> cls) {
        return (T) map.get(cls);
    }
}
