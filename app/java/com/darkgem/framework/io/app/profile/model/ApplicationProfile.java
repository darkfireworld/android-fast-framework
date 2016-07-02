package com.darkgem.framework.io.app.profile.model;

import android.support.annotation.Nullable;

/**
 * Created by Administrator on 2016/1/12.
 */
public class ApplicationProfile {
    @Nullable
    String usrId;
    @Nullable
    String mainToken;
    //如果没有，则使用0代替
    int version;

    public ApplicationProfile(String usrId, String mainToken, int version) {
        this.usrId = usrId;
        this.mainToken = mainToken;
        this.version = version;
    }

    @Nullable
    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(@Nullable String usrId) {
        this.usrId = usrId;
    }

    @Nullable
    public String getMainToken() {
        return mainToken;
    }

    public void setMainToken(@Nullable String mainToken) {
        this.mainToken = mainToken;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
