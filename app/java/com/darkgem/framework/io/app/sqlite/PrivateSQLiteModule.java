package com.darkgem.framework.io.app.sqlite;

import android.content.Context;
import com.darkgem.framework.io.app.ApplicationIo;
import com.darkgem.framework.io.notification.sqlite.NotificationProfileSQLite;
import com.darkgem.framework.io.support.AbsSQLite;
import com.darkgem.framework.io.support.AbsSQLiteHelper;
import com.darkgem.framework.support.kit.AppKit;
import com.darkgem.framework.support.kit.FileKit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 私有数据
 */
public class PrivateSQLiteModule extends AbsSQLiteHelper {
    //数据库名称
    static final String DB_NAME = "private.db";
    //密钥
    static final String KEY = "www.android.com";

    public PrivateSQLiteModule(Context context) {
        super(context,
                FileKit.getAppUsrDir(context, ApplicationIo.getInstance(context).getUsrId(), FileKit.AppUsrDirType.DB) + File.separator + DB_NAME,
                AppKit.getVersionCodeInApk(context),
                KEY + ApplicationIo.getInstance(context).getUsrId());
    }

    @Override
    protected <T extends AbsSQLite> List<Class<? extends AbsSQLite>> registerAbsDao() {
        List<Class<? extends AbsSQLite>> ret = new ArrayList<Class<? extends AbsSQLite>>();
        //通知
        ret.add(NotificationProfileSQLite.class);
        return ret;
    }
}
