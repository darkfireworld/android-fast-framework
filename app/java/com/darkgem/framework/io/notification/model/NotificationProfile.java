package com.darkgem.framework.io.notification.model;

/**
 * Created by Administrator on 2016/1/13.
 */
public class NotificationProfile {
    //是否提示
    boolean alert;
    //是否显示详情
    boolean detail;
    //是否发出声音
    boolean voice;
    //是否震动
    boolean shake;

    public NotificationProfile(boolean alert, boolean detail, boolean voice, boolean shake) {
        this.alert = alert;
        this.detail = detail;
        this.voice = voice;
        this.shake = shake;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public boolean isDetail() {
        return detail;
    }

    public void setDetail(boolean detail) {
        this.detail = detail;
    }

    public boolean isVoice() {
        return voice;
    }

    public void setVoice(boolean voice) {
        this.voice = voice;
    }

    public boolean isShake() {
        return shake;
    }

    public void setShake(boolean shake) {
        this.shake = shake;
    }
}
