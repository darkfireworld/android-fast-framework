package com.darkgem.framework.io.app.profile;

import android.content.Context;
import android.support.annotation.NonNull;
import com.darkgem.framework.io.app.ApplicationIo;
import com.darkgem.framework.io.app.profile.model.ApplicationProfile;
import com.darkgem.framework.io.app.profile.sqlite.ApplicationProfileSQLite;

/**
 * ApplicationProfileModule
 */
public class ApplicationProfileModule {

    Context context;

    public ApplicationProfileModule(Context context) {
        this.context = context;
    }

    /**
     * 记录APP版本号到数据库中
     */
    public void setVersionCodeInDb(int version) {
        ApplicationProfile applicationProfile = select();
        //记录
        applicationProfile.setVersion(version);
        //保存
        ApplicationIo.getInstance(context).getPublicSQLite(ApplicationProfileSQLite.class).merge(applicationProfile);
    }

    /**
     * 搜索 TOKEN 到Db
     */
    public void recordToken(String usrId, String mainToken) {
        ApplicationProfile applicationProfile = select();
        //登入用户ID
        applicationProfile.setUsrId(usrId);
        //主服务TOKEN
        applicationProfile.setMainToken(mainToken);
        //保存
        ApplicationIo.getInstance(context).getPublicSQLite(ApplicationProfileSQLite.class).merge(applicationProfile);
    }

    /**
     * 清空所有的TOKEN信息
     */
    public void clearToken() {
        ApplicationProfile applicationProfile = select();
        //清空主服务TOKEN
        applicationProfile.setMainToken(null);
        //保存
        ApplicationIo.getInstance(context).getPublicSQLite(ApplicationProfileSQLite.class).merge(applicationProfile);
    }

    /**
     * 判断TOKEN是否可用，也就是可以认为是否登入成功
     */
    public boolean readyToken() {
        ApplicationProfile applicationProfile = select();
        if (applicationProfile.getUsrId() == null
                || applicationProfile.getMainToken() == null) {
            return false;
        }
        return true;
    }

    /**
     * 读取 SYSTEM0
     */
    @NonNull
    public ApplicationProfile select() {
        ApplicationProfile applicationProfile = ApplicationIo.getInstance(context).getPublicSQLite(ApplicationProfileSQLite.class).select();
        if (applicationProfile == null) {
            applicationProfile = new ApplicationProfile(null, null, 0);
        }
        return applicationProfile;
    }
}
