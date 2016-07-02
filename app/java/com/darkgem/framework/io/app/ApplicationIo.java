package com.darkgem.framework.io.app;

import android.content.Context;
import android.support.annotation.Nullable;
import com.darkgem.framework.activity.splash.SplashActivity;
import com.darkgem.framework.io.app.initialize.InitializeModule;
import com.darkgem.framework.io.app.profile.ApplicationProfileModule;
import com.darkgem.framework.io.app.scheduled.ScheduledModule;
import com.darkgem.framework.io.app.sqlite.PrivateSQLiteModule;
import com.darkgem.framework.io.app.sqlite.PublicSQLiteModule;
import com.darkgem.framework.io.support.AbsSQLite;
import com.darkgem.framework.support.kit.AppKit;

import java.util.concurrent.TimeUnit;

/**
 * 系统核心模块
 */
public class ApplicationIo {


    //单例
    static volatile ApplicationIo instance;
    //上下文
    volatile Context context;
    //初始化模块
    volatile InitializeModule initializeModule;
    //系统配置模块
    volatile ApplicationProfileModule applicationProfileModule;
    //系统周期性调度
    volatile ScheduledModule scheduledModule;
    //SQL支持模块,延迟初始化，主要是为了避免 USR ID 不存在导致打开 私有数据库出异常
    //私有数据库
    volatile PrivateSQLiteModule privateSQLiteModule;
    //公用数据库
    volatile PublicSQLiteModule publicSQLiteModule;

    public ApplicationIo(Context context) {
        this.context = context;
        this.initializeModule = new InitializeModule(context);
        this.applicationProfileModule = new ApplicationProfileModule(context);
        this.scheduledModule = new ScheduledModule(context);
    }

    /**
     * 获取实例
     */
    public static ApplicationIo getInstance(Context context) {
        if (instance == null) {
            synchronized (ApplicationIo.class) {
                if (instance == null) {
                    instance = new ApplicationIo(context);
                }
            }
        }
        return instance;
    }

    /**
     * 初始化系统模块
     */
    public void tryInitializeCore() {
        initializeModule.tryInitializeCore();
    }

    /**
     * 获取当前登入用户ID
     */
    @Nullable
    public String getUsrId() {
        return applicationProfileModule.select().getUsrId();
    }

    /**
     * 读取主服务TOKEN
     */
    @Nullable
    public String getMainToken() {
        return applicationProfileModule.select().getMainToken();
    }

    /**
     * 读取存储在数据库中的APP 版本号
     */
    public int getVersionCodeInDb() {
        return applicationProfileModule.select().getVersion();
    }

    /**
     * 设置App 版本号到Db中
     */
    public void setVersionCodeInDb(int version) {
        applicationProfileModule.setVersionCodeInDb(version);
    }

    /**
     * 记录TOKEN到DB中
     */
    public void recordToken(String usrId, String mainToken) {
        applicationProfileModule.recordToken(usrId, mainToken);
    }

    /**
     * 清空Token
     */
    public void clearToken() {
        applicationProfileModule.clearToken();
    }

    /**
     * 判断TOKEN是否可用，也就是可以认为是否登入成功
     */
    public boolean isLogin() {
        return applicationProfileModule.readyToken();
    }

    /**
     * 杀死应用
     */
    public void kill(boolean restart) {
        AppKit.kill(context, restart, SplashActivity.class);
    }

    /**
     * 获取调度线程池
     */
    public void schedule(Runnable runnable, long delay, TimeUnit unit) {
        scheduledModule.schedule(runnable, delay, unit);
    }

    /**
     * 获取公用SQLite
     */
    public <T extends AbsSQLite> T getPublicSQLite(Class<T> cls) {
        if (publicSQLiteModule == null) {
            synchronized (this) {
                if (publicSQLiteModule == null) {
                    publicSQLiteModule = new PublicSQLiteModule(context);
                }
            }
        }
        return publicSQLiteModule.getSQLite(cls);
    }

    /**
     * 获取私用SQLite
     */
    public <T extends AbsSQLite> T getPrivateSQLite(Class<T> cls) {
        if (privateSQLiteModule == null) {
            synchronized (this) {
                if (privateSQLiteModule == null) {
                    privateSQLiteModule = new PrivateSQLiteModule(context);
                }
            }
        }
        return privateSQLiteModule.getSQLite(cls);
    }

}
