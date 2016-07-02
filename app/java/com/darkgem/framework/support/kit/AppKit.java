package com.darkgem.framework.support.kit;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.*;
import android.support.annotation.Nullable;
import android.util.Log;
import com.umeng.analytics.MobclickAgent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 系统工具
 */
public class AppKit {
    /**
     * 获取当前进程的名称
     */
    @Nullable
    static public String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Iterator iterator = mActivityManager.getRunningAppProcesses().iterator();

        ActivityManager.RunningAppProcessInfo appProcess;
        do {
            if (!iterator.hasNext()) {
                return null;
            }
            appProcess = (ActivityManager.RunningAppProcessInfo) iterator.next();
        } while (appProcess.pid != pid);

        return appProcess.processName;
    }

    /**
     * 重启App
     */
    static public void kill(Context context, boolean restart, Class<? extends Activity> launchClz) {
        //重启
        if (restart) {
            Intent intent = new Intent(context, launchClz);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        //停止后台 service
        try {
            //读取所有的SERVICE信息
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES);
            if (packageInfo != null && packageInfo.services != null) {
                for (ServiceInfo serviceInfo : packageInfo.services) {
                    try {
                        Class cls = Class.forName(serviceInfo.name);
                        context.stopService(new Intent(context, cls));
                    } catch (ClassNotFoundException e) {
                    }
                }
            }
        } catch (Exception e) {
            Log.e(AppKit.class.getName(), e.getMessage(), e);
        }
        //杀死所有进程
        try {
            Set<String> processNameSet = new HashSet<String>();
            //读取 所有的Process
            {
                //读取Activity
                {
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
                    if (packageInfo != null && packageInfo.activities != null) {
                        for (ActivityInfo activityInfo : packageInfo.activities) {
                            processNameSet.add(activityInfo.processName);
                        }
                    }
                }
                //读取Service
                {
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES);
                    if (packageInfo != null && packageInfo.services != null) {
                        for (ServiceInfo serviceInfo : packageInfo.services) {
                            processNameSet.add(serviceInfo.processName);
                        }
                    }
                }
                //读取 RECEIVERS
                {
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_RECEIVERS);
                    if (packageInfo != null && packageInfo.receivers != null) {
                        for (ActivityInfo activityInfo : packageInfo.receivers) {
                            processNameSet.add(activityInfo.processName);
                        }
                    }
                }
                //读取 PROVIDERS
                {
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
                    if (packageInfo != null && packageInfo.providers != null) {
                        for (ProviderInfo providerInfo : packageInfo.providers) {
                            processNameSet.add(providerInfo.processName);
                        }
                    }
                }
            }
            //关闭所有进程
            {
                int myPid = android.os.Process.myPid();
                ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                Iterator iterator = mActivityManager.getRunningAppProcesses().iterator();
                while (iterator.hasNext()) {
                    ActivityManager.RunningAppProcessInfo appProcess = (ActivityManager.RunningAppProcessInfo) iterator.next();
                    if (processNameSet.contains(appProcess.processName)) {
                        //先KILL其他进程
                        if (appProcess.pid != myPid) {
                            android.os.Process.killProcess(appProcess.pid);
                        }
                    }
                }
                //保存统计记录
                MobclickAgent.onKillProcess(context);
                //杀死自己
                android.os.Process.killProcess(myPid);
            }
        } catch (Exception e) {
            Log.e(AppKit.class.getName(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取手机版本信息
     */
    static public String getPlatform() {
        StringBuilder sb = new StringBuilder("Android:");
        sb.append(android.os.Build.VERSION.RELEASE);
        sb.append(" MODEL:");
        sb.append(android.os.Build.MODEL);
        return sb.toString();
    }

    /**
     * 获取AndroidManifest.xml 中的versioncode, <strong> 如果出现任何的异常，则返回 0</strong>
     */
    static public int getVersionCodeInApk(Context context) {
        int versionCode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AppKit.class.getName(), e.getMessage(), e);
        }
        return versionCode;
    }

    /**
     * 获取AndroidManifest.xml 中的versionname, <strong>如果出现任何的异常，返回null</strong>
     */
    @Nullable
    static public String getVersionNameInApk(Context context) {
        String versionName = null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(AppKit.class.getName(), e.getMessage(), e);
        }
        return versionName;
    }
}
