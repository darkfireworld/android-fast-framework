package com.darkgem.framework.io.upgrade.model;

/**
 * android 更新
 */
public class Upgrade {
    int versionCode;
    String versionName;
    int deadVersionCode;
    String description;
    String download;
    long ct;

    public Upgrade(int versionCode, String versionName, int deadVersionCode, String description, String download, long ct) {
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.deadVersionCode = deadVersionCode;
        this.description = description;
        this.download = download;
        this.ct = ct;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getDeadVersionCode() {
        return deadVersionCode;
    }

    public void setDeadVersionCode(int deadVersionCode) {
        this.deadVersionCode = deadVersionCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public long getCt() {
        return ct;
    }

    public void setCt(long ct) {
        this.ct = ct;
    }
}
