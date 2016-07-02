package com.darkgem.framework.support.kit;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * 文件工具
 */
public class FileKit {
    /**
     * APP 的主存储上的目录
     */
    public enum AppDirTypeInMain {
        CACHE("cache"),
        USR("usr"),
        DB("db");
        String dir;

        AppDirTypeInMain(String dir) {
            this.dir = dir;
        }

        public String getDir() {
            return dir;
        }
    }

    /**
     * APP 在SD卡上的目录
     */
    public enum AppDirTypeInExt {
        CACHE("cache"),
        IMAGE("image"),
        UPLOAD("upload"),
        INSTALL("install");
        String dir;

        AppDirTypeInExt(String dir) {
            this.dir = dir;
        }

        public String getDir() {
            return dir;
        }
    }

    /**
     * 在主存上，用户私有的目录
     */
    public enum AppUsrDirType {
        DB("db");
        String dir;

        AppUsrDirType(String dir) {
            this.dir = dir;
        }

        public String getDir() {
            return dir;
        }
    }

    /**
     * 清空某个文件下所有的文件
     */
    public static void deleteDir(String filePath) {
        try {
            if (filePath == null) {
                return;
            }
            File file = new File(filePath);
            if (file.exists() && file.isDirectory()) {
                //若目录下没有文件则直接删除
                if (file.listFiles().length == 0) {
                    file.delete();
                } else {
                    //若有则把文件放进数组，并判断是否有下级目录
                    File delFile[] = file.listFiles();
                    int i = file.listFiles().length;
                    for (int j = 0; j < i; j++) {
                        if (delFile[j].isDirectory()) {
                            //递归调用del方法并取得子目录路径
                            deleteDir(delFile[j].getAbsolutePath());
                        }
                        //删除文件
                        delFile[j].delete();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(FileKit.class.getName(), e.getMessage(), e);
        }
    }


    /**
     * 获取APP的主目录，在主存卡
     */
    public static String getAppDirInMain(Context context, AppDirTypeInMain appDirTypeInMain) {
        //创建 Main SD 卡上，关于该App的 ROOT 目录
        File appRootDir = context.getDir("bucket", Context.MODE_PRIVATE);
        if (!appRootDir.exists()) {
            appRootDir.mkdirs();
        }
        //创建具体的类型目录
        File dir = new File(appRootDir, appDirTypeInMain.getDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    /**
     * 获取用户的私有目录，在主存上
     */
    public static String getAppUsrDir(Context context, String usrId, AppUsrDirType appUsrDirType) {
        //检测USR_ID
        if (usrId == null) {
            throw new NullPointerException("USR ID IS NULL");
        }
        //创建私有用户目录
        File usrDir = new File(getAppDirInMain(context, AppDirTypeInMain.USR), usrId);
        if (!usrDir.exists())
            usrDir.mkdirs();
        //创建具体的类型目录
        File dir = new File(usrDir, appUsrDirType.getDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    /**
     * 获取app sd 卡上的目录,  如果SD卡出问题, 则返回null
     */
    public static String getAppDirInExt(Context context, AppDirTypeInExt type) throws NotFoundExternalSD {

        try {
            //如果没有挂载，或者没有写入外部磁盘权限，则抛出异常
            if (!"mounted".equals(Environment.getExternalStorageState())
                    || context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                throw new RuntimeException("NO SD MOUNTED");
            }
            //外部存储路劲
            String externalStorePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            //如果找不到外部存储, 则抛出异常
            if (TextUtils.isEmpty(externalStorePath)) {
                throw new NullPointerException("externalStorePath is null");
            }
            //在SD卡上建立APP主目录
            File appDirInExt = new File(externalStorePath, context.getPackageName());
            if (!appDirInExt.exists()) {
                appDirInExt.mkdirs();
            }
            //创建具体的TYPE目录
            File dir = new File(appDirInExt, type.getDir());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir.getAbsolutePath();
        } catch (Exception e) {
            Log.e(FileKit.class.getName(), e.getMessage(), e);
            throw new NotFoundExternalSD(e);
        }
    }

    /**
     * 获取缓存文件夹地址, 依次读取一下的目录
     * 1. 外部 app_home->cache目录
     * 2. 内部 app_home->cache目录
     */
    public static String getAppCacheDir(Context context) {
        //读取APP 在 SD卡上主要的CACHE目录
        try {
            return getAppDirInExt(context, AppDirTypeInExt.CACHE);
        } catch (NotFoundExternalSD e) {
            Log.e(FileKit.class.getName(), e.getMessage(), e);
        }
        //读取 Android 在Main Store 上创建的CACHE目录
        return getAppDirInMain(context, AppDirTypeInMain.CACHE);
    }


    /**
     * 没有外部存储
     */
    public static class NotFoundExternalSD extends Exception {
        public NotFoundExternalSD(Exception e) {
            super(e);
        }
    }
}
