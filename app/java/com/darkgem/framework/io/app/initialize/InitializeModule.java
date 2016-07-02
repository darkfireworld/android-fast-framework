package com.darkgem.framework.io.app.initialize;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;
import com.darkgem.framework.io.app.ApplicationIo;
import com.darkgem.framework.io.event.AlarmEvent;
import com.darkgem.framework.io.event.LogoutEvent;
import com.darkgem.framework.io.event.MessageEvent;
import com.darkgem.framework.io.event.ScreenEvent;
import com.darkgem.framework.io.kv.KvIo;
import com.darkgem.framework.io.notification.NotificationIo;
import com.darkgem.framework.io.upgrade.UpgradeIo;
import com.darkgem.framework.service.KeepAliveService;
import com.darkgem.framework.service.ScreenReceiver;
import com.darkgem.framework.support.kit.AppKit;
import com.darkgem.framework.support.kit.FileKit;
import com.darkgem.framework.support.kit.ThreadPoolKit;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.umeng.analytics.AnalyticsConfig;
import de.greenrobot.event.EventBus;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.concurrent.TimeUnit;

/**
 * 初始化模块
 */
public class InitializeModule {
    //清空缓存的时间
    static final long CLEAR_CACHE_TIME = 1000 * 3600 * 24 * 7;
    Context context;
    //初始化标志-阶段1
    boolean initializedCoreState1 = false;
    //初始化标志-阶段2
    boolean initializedCoreState2 = false;


    public InitializeModule(Context context) {
        this.context = context;
    }

    /**
     * 初始化系统模块,且<strong>必须在主线程进行</strong>
     */
    public synchronized void tryInitializeCore() {
        //如果不在主线程，或者不在主进程,则忽略
        if (!context.getPackageName().equals(AppKit.getProcessName(context))
                && Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException(String.format("tryInitializeCore should in Main"));
        }
        Log.d(InitializeModule.class.getName(), "Call App#tryInitializeCore()");
        //初始化-阶段1
        tryInitializeCoreStage1();
        //初始化-阶段2
        tryInitializeCoreStage2();
    }

    /**
     * 初始化系统模块
     */
    void tryInitializeCoreStage1() {
        //已经初始化，则忽略
        if (initializedCoreState1) {
            return;
        }
        //初始化系统级别的内容
        Log.d(InitializeModule.class.getName(), "Call App#tryInitializeCoreStage1()");
        //初始化
        {
            //初始化统计
            {
                //日志加密
                AnalyticsConfig.enableEncrypt(true);
            }
            //设置全局异常处理
            {
                //重启
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread thread, Throwable ex) {
                        Log.e(thread.getName(), ex.getMessage(), ex);
                        ApplicationIo.getInstance(context).kill(true);
                    }
                });
            }
            //数据库
            {
                //加载加密数据库lib
                SQLiteDatabase.loadLibs(context);
            }
            //版本控制
            {
                //默认版本号
                int appVersion = AppKit.getVersionCodeInApk(context);
                //更新
                int oldVer = ApplicationIo.getInstance(context).getVersionCodeInDb();
                if (oldVer < appVersion) {
                    //只有之前安装过App的，才能进行升级
                    //没有安装过App的versionCode为0
                    if (oldVer > 0) {
                        onUpgrade(oldVer, appVersion);
                    }
                    //更新数据库中的版本信息
                    ApplicationIo.getInstance(context).setVersionCodeInDb(appVersion);
                }
            }
            //兼容性问题解决
            {
                //Android2.3 启动 如果在子线程启动第一个AsyncTask的话，会造成无法启用的错误
                {
                    AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            return null;
                        }
                    };
                    asyncTask.execute();
                }
            }
            //初始化imageLoad
            {
                ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(context)
                                .taskExecutor(ThreadPoolKit.createThreadPoolExecutor(1, 4, 60, TimeUnit.SECONDS, "uil-image-loader-normal"))
                                .taskExecutorForCachedImages(ThreadPoolKit.createThreadPoolExecutor(1, 4, 60, TimeUnit.SECONDS, "uil-image-loader-cache"))
                                .diskCacheSize(1024 * 1024 * 64)
                                .build()
                );
            }
            //初始化事件
            {
                //EVENT BUS
                {
                    //初始化EventBus
                    EventBus.builder().executorService(ThreadPoolKit.createThreadPoolExecutor(1, 2, 60l, TimeUnit.SECONDS, "app-event-bus")).installDefaultEventBus();
                    EventBus.getDefault().register(new Object() {
                        //锁屏
                        boolean lockScreen = false;

                        /**
                         * 处理消息
                         * */
                        public void onEventMainThread(MessageEvent event) {

                        }

                        /**
                         * 锁屏事件
                         * */
                        public void onEventMainThread(ScreenEvent event) {
                            lockScreen = event.isLock();
                        }

                        /**
                         * 登出处理
                         * */
                        public void onEventMainThread(LogoutEvent type) {
                            if (type == LogoutEvent.TOKEN_INVALID) {
                                Toast.makeText(context, "登入失效，请重新登录App", Toast.LENGTH_LONG).show();
                            }
                            if (type == LogoutEvent.FORCE_UPGRADE) {
                                Toast.makeText(context, "当前版本号过低，请下载最新的App", Toast.LENGTH_LONG).show();
                            }
                            if (type == LogoutEvent.PUSH_CONNECT_INVALID) {
                                Toast.makeText(context, "PUSH服务器连接失败", Toast.LENGTH_LONG).show();
                            }
                            //清空用户登入信息
                            ApplicationIo.getInstance(context).clearToken();
                            //TODO 断开连接服务

                            //清空通知
                            NotificationIo.getInstance(context).clearNotification();
                            //重启
                            ApplicationIo.getInstance(context).kill(true);
                        }
                    }, 0);
                }

                //注册锁屏
                {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_SCREEN_ON);
                    filter.addAction(Intent.ACTION_SCREEN_OFF);
                    context.registerReceiver(new ScreenReceiver(), filter);
                }

                //初始化闹钟
                {
                    try {
                        //注册监听器
                        {
                            IntentFilter filter = new IntentFilter(InitializeModule.class.getName());
                            filter.addCategory(Intent.CATEGORY_DEFAULT);
                            context.registerReceiver(new BroadcastReceiver() {
                                long count = 0;

                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    //发送事件
                                    count++;
                                    EventBus.getDefault().post(new AlarmEvent(count));
                                }
                            }, filter);
                        }
                        //5分钟调用一次
                        {
                            Intent intent = new Intent();
                            intent.setAction(InitializeModule.class.getName());
                            intent.addCategory(Intent.CATEGORY_DEFAULT);

                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                            //按照手机时间流逝进行调用
                            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * 60 * 5, 1000 * 60 * 5, pendingIntent);
                        }
                    } catch (Exception e) {
                        Log.e(InitializeModule.class.getName(), e.getMessage(), e);
                    }
                }
            }
            //开启保活Service
            {
                context.startService(new Intent(context, KeepAliveService.class));
            }
            //初始化后台任务
            {
                //清空缓存
                clearCachePlan();
                //检测升级
                UpgradeIo.getInstance(context);
            }
        }
        //标记初始化成功
        initializedCoreState1 = true;

    }


    /**
     * 初始化用户相关模块
     */
    void tryInitializeCoreStage2() {
        //已经初始化，或者未登入,则忽略
        if (initializedCoreState2
                || !ApplicationIo.getInstance(context).isLogin()) {
            return;
        }
        Log.d(InitializeModule.class.getName(), "Call App#tryInitializeCoreStage2()");
        //初始化
        {

        }
        //标记初始化成功
        initializedCoreState2 = true;
    }

    /**
     * App 版本升级
     *
     * @param oldVer     之前存储在数据库中的版本号
     * @param appVersion 现在APP的版本号
     */
    private void onUpgrade(int oldVer, int appVersion) {
        for (int ver = oldVer + 1; ver <= appVersion; ++ver) {
            switch (ver) {

            }
        }
    }

    //清除缓存
    void clearCachePlan() {
        try {
            //上次清空时间和当前时间差值超过7天的时候，需要进行清空缓存操作
            if (Math.abs(System.currentTimeMillis() - KvIo.getInstance(context).getKv(KvIo.ProfileKv.class).getLastTimeForClearCache()) > CLEAR_CACHE_TIME) {
                //清空Cache目录
                String cacheDir = FileKit.getAppCacheDir(context);
                FileKit.deleteDir(cacheDir);
                //清空webView Cache
                WebView webView = new WebView(context);
                webView.clearCache(true);
                //记录本次清空时间
                KvIo.getInstance(context).getKv(KvIo.ProfileKv.class).setLastTimeForClearCache(System.currentTimeMillis());
            }
        } catch (Exception e) {
            Log.e(InitializeModule.class.getName(), e.getMessage(), e);
        }
    }
}
