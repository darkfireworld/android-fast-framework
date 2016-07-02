package com.darkgem.framework.io.app.sqlite;

import android.content.Context;
import com.darkgem.framework.io.app.profile.sqlite.ApplicationProfileSQLite;
import com.darkgem.framework.io.support.AbsSQLite;
import com.darkgem.framework.io.support.AbsSQLiteHelper;
import com.darkgem.framework.support.kit.AppKit;
import com.darkgem.framework.support.kit.FileKit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 公用数据
 */
public class PublicSQLiteModule extends AbsSQLiteHelper {
    //数据库名称
    static final String DB_NAME = "public.db";
    //密钥
    static final String KEY = "www.android.com";

    public PublicSQLiteModule(Context context) {
        super(context,
                FileKit.getAppDirInMain(context, FileKit.AppDirTypeInMain.DB) + File.separator + DB_NAME,
                AppKit.getVersionCodeInApk(context),
                KEY);
    }

    @Override
    protected <T extends AbsSQLite> List<Class<? extends AbsSQLite>> registerAbsDao() {
        List<Class<? extends AbsSQLite>> ret = new ArrayList<Class<? extends AbsSQLite>>();
        ret.add(ApplicationProfileSQLite.class);
        return ret;
    }
}
