package com.darkgem.framework.io.upgrade;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.darkgem.framework.io.app.ApplicationIo;
import com.darkgem.framework.io.event.DownloadEvent;
import com.darkgem.framework.io.event.LogoutEvent;
import com.darkgem.framework.io.http.OkHttpIo;
import com.darkgem.framework.io.kv.KvIo;
import com.darkgem.framework.io.upgrade.model.Upgrade;
import com.darkgem.framework.support.callback.Callback;
import com.darkgem.framework.support.kit.AppKit;
import com.darkgem.framework.support.kit.FileKit;
import com.darkgem.framework.support.kit.NetworkKit;
import com.darkgem.framework.support.tuple.Tuple2;
import de.greenrobot.event.EventBus;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class UpgradeIo {
    final static long CHECK_TIME = 1000 * 3600 * 23;
    static volatile UpgradeIo instance;
    Context context;
    //被缓存的版本信息
    Upgrade upgradeInCache;
    //正在处于下载的id，来自于DownloadManager
    Set<Long> downloadIds = new HashSet<Long>();

    public UpgradeIo(Context context) {
        this.context = context;
        //注册监听下载完成事件
        initEvent();
        //启动定时更新线程
        initScheduled();
    }

    private void initEvent() {
        EventBus.getDefault().register(this);
    }

    public static UpgradeIo getInstance(Context context) {
        if (instance == null) {
            synchronized (UpgradeIo.class) {
                if (instance == null) {
                    instance = new UpgradeIo(context);
                }
            }
        }
        return instance;
    }

    /**
     * 处理下载通知事件
     */
    public void onEventMainThread(DownloadEvent event) {
        //安装
        installApp(event.getId(), event.getFileName());
        //提示升级
        Toast.makeText(context, "开始安装App", Toast.LENGTH_SHORT).show();
    }

    /**
     * 周期性更新最近版本信息
     */
    private synchronized void initScheduled() {
        new Runnable() {

            @Override
            public void run() {
                //如果是初始化, 则不进行调用sync接口
                try {
                    if (Math.abs(System.currentTimeMillis() - KvIo.getInstance(context).getKv(KvIo.ProfileKv.class).getLastTimeForSyncAppVersion()) > CHECK_TIME) {
                        checkAppVersion();
                    }
                } catch (Exception e) {
                    Log.e(UpgradeIo.class.getName(), e.getMessage(), e);
                }
                //加入调度队列
                ApplicationIo.getInstance(context).schedule(this, 24, TimeUnit.HOURS);
            }
        }.run();
    }

    /**
     * 检测App是否可以正常使用
     */
    private synchronized void checkAppVersion() {
        checkAppUsable(true, new Callback<Boolean, Void>() {
            @Override
            public void onSuccess(Boolean ok) {
                if (ok == null || !ok) {
                    EventBus.getDefault().post(LogoutEvent.FORCE_UPGRADE);
                }
            }

            @Override
            public void onError(Void error) {

            }
        });
    }


    /**
     * 检测当前版本是否还可以使用，注意非最新
     *
     * @param forceNet 是否强制从网络获取最新的消息
     * @param callback 回调函数
     */
    public synchronized void checkAppUsable(boolean forceNet, final Callback<Boolean, Void> callback) {
        getAppUpgrade(forceNet, new Callback<Upgrade, Void>() {
            @Override
            public void onSuccess(Upgrade upgrade) {
                int avc = AppKit.getVersionCodeInApk(context);
                int dvc = upgrade.getDeadVersionCode();
                if (callback != null) {
                    callback.onSuccess(avc > dvc);
                }
            }

            @Override
            public void onError(Void error) {
                if (callback != null) {
                    //如果发生任何异常，都默认版本可用，避免因为网络失败，而导致提示异常
                    callback.onSuccess(true);
                }
            }
        });
    }

    /**
     * 获取版本信息, 如果没有，则从服务器获取
     *
     * @param forceNet 是否强制从网络获取
     * @param callback 回调
     */
    public synchronized void getAppUpgrade(boolean forceNet, final Callback<Upgrade, Void> callback) {
        if (upgradeInCache != null && !forceNet) {
            if (callback != null) {
                callback.onSuccess(upgradeInCache);
            }
        } else {
            getAppUpgradeInNetwork(callback);
        }
    }

    /**
     * 从服务器获取最新的版本信息
     */
    private synchronized void getAppUpgradeInNetwork(final Callback<Upgrade, Void> callback) {
        OkHttpIo.getInstance(context).post("http://im.kxlapp.com/v1/upgrade/UpgradeCtrl/androidUpgrade.do", null, new Callback<Tuple2<Integer, String>, Exception>() {
            @Override
            public void onSuccess(Tuple2<Integer, String> res) {
                JSONObject json = JSON.parseObject(res._2).getJSONObject("msg");
                Integer versionCode = json.getInteger("versionCode");
                String versionName = json.getString("versionName");
                Integer deadVersionCode = json.getIntValue("deadVersionCode");
                String description = json.getString("description");
                String download = json.getString("download");
                long ct = json.getLongValue("ct");
                Upgrade upgrade = new Upgrade(versionCode, versionName, deadVersionCode, description, download, ct);
                //记录到cache中
                upgradeInCache = upgrade;
                //记录最后同步App版本信息时间
                KvIo.getInstance(context).getKv(KvIo.ProfileKv.class).setLastTimeForSyncAppVersion(System.currentTimeMillis());
                if (callback != null) {
                    callback.onSuccess(upgrade);
                }
            }

            @Override
            public void onError(Exception error) {
                if (callback != null) {
                    callback.onError(null);
                }
            }
        });
    }


    /**
     * 指定URL 下载 APP
     *
     * @param httpUrl apk地址
     */
    public synchronized void downloadApp(final String httpUrl, final Callback<String, DownloadError> callback) {
        //检测网络
        if (!NetworkKit.isNetworkAvailable(context)) {
            if (callback != null) {
                callback.onError(DownloadError.NETWORK_ERR);
            }
            return;
        }
        //2G 网络不允许更新
        if (NetworkKit.getNetType(context) == NetworkKit.Type.MOBILE_2G_NET) {
            if (callback != null) {
                callback.onError(DownloadError.NETWORK_ERR);
            }
            return;
        }
        //获取外置磁盘中install位置
        String installDirPath;
        try {
            installDirPath = FileKit.getAppDirInExt(context, FileKit.AppDirTypeInExt.INSTALL);
        } catch (FileKit.NotFoundExternalSD e) {
            Log.e(UpgradeIo.class.getName(), e.getMessage(), e);
            if (callback != null) {
                callback.onError(DownloadError.EXTERNAL_STORAGE_ERR);
            }
            return;
        }
        //清空目录
        FileKit.deleteDir(installDirPath);
        File installDir = new File(installDirPath);
        installDir.mkdirs();
        //开始下载
        try {
            String serviceString = Context.DOWNLOAD_SERVICE;
            DownloadManager downloadManager;
            downloadManager = (DownloadManager) context.getSystemService(serviceString);
            Uri uri = Uri.parse(httpUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            //APK名称
            request.setTitle(context.getPackageName() + ".apk");
            //设置名称
            request.setDescription(context.getPackageName() + "正在下载中...");
            //设置MimeType
            request.setMimeType("application/vnd.android.package-archive");
            //下载位置
            File apkFile = new File(installDir, UUID.randomUUID().toString() + ".apk");
            request.setDestinationUri(Uri.fromFile(apkFile));
            //设置样式
            request.setVisibleInDownloadsUi(true);
            //设置下载进度可见
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            //开始下载
            long id = downloadManager.enqueue(request);
            downloadIds.add(id);
            //回调成功
            if (callback != null) {
                callback.onSuccess(apkFile.getAbsolutePath());
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(DownloadError.DOWNLOAD_ERR);
            }
        }

    }


    /**
     * 安装App
     *
     * @param id   来之DownloadManager的任务ID,如果为 null, 则不检验 是否为任务id
     * @param path 路径地址
     */
    public synchronized boolean installApp(long id, String path) {
        if (downloadIds.contains(id)) {
            File apkFile = new File(path);
            if (!apkFile.exists()) {
                return false;
            }
            // 通过Intent安装APK文件
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            //清空下载任务id
            downloadIds.remove(id);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 下载APK异常
     */
    public enum DownloadError {
        //网络错误
        NETWORK_ERR,
        //无外部SD
        EXTERNAL_STORAGE_ERR,
        //下载错误
        DOWNLOAD_ERR
    }
}
