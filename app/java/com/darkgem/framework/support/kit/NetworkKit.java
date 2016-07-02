package com.darkgem.framework.support.kit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkKit {

    /**
     * Network type is unknown
     */
    static final int NETWORK_TYPE_UNKNOWN = 0;
    /**
     * Current network is GPRS
     */
    static final int NETWORK_TYPE_GPRS = 1;
    /**
     * Current network is EDGE
     */
    static final int NETWORK_TYPE_EDGE = 2;
    /**
     * Current network is UMTS
     */
    static final int NETWORK_TYPE_UMTS = 3;
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    static final int NETWORK_TYPE_CDMA = 4;
    /**
     * Current network is EVDO revision 0
     */
    static final int NETWORK_TYPE_EVDO_0 = 5;
    /**
     * Current network is EVDO revision A
     */
    static final int NETWORK_TYPE_EVDO_A = 6;
    /**
     * Current network is 1xRTT
     */
    static final int NETWORK_TYPE_1xRTT = 7;
    /**
     * Current network is HSDPA
     */
    static final int NETWORK_TYPE_HSDPA = 8;
    /**
     * Current network is HSUPA
     */
    static final int NETWORK_TYPE_HSUPA = 9;
    /**
     * Current network is HSPA
     */
    static final int NETWORK_TYPE_HSPA = 10;
    /**
     * Current network is iDen
     */
    static final int NETWORK_TYPE_IDEN = 11;
    /**
     * Current network is EVDO revision B
     */
    static final int NETWORK_TYPE_EVDO_B = 12;
    /**
     * Current network is LTE
     */
    static final int NETWORK_TYPE_LTE = 13;
    /**
     * Current network is eHRPD
     */
    static final int NETWORK_TYPE_EHRPD = 14;
    /**
     * Current network is HSPA+
     */
    static final int NETWORK_TYPE_HSPAP = 15;

    /**
     * 检查是否有网络
     */
    public static boolean isNetworkAvailable(Context context) {
        return getNetType(context) != Type.INVALID_NET;
    }

    /**
     * 检查是否是WIFI
     */
    public static boolean isWifi(Context context) {
        return getNetType(context) == Type.WIFI_NET;
    }

    /**
     * 获取网络类型
     */
    public static Type getNetType(Context context) {
        //获取当前网络连接
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return Type.INVALID_NET;
        }
        //获取网络信息
        NetworkInfo info = cm.getActiveNetworkInfo();
        //判断网络是否有效
        if (info == null) {
            return Type.INVALID_NET;
        }
        if (!info.isAvailable()) {
            return Type.INVALID_NET;
        }
        //判断是否为Wifi网络
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            return Type.WIFI_NET;
        }
        //检测移动网络类型
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                //来自于 getNetType
                switch (telephonyManager.getNetworkType()) {
                    case NETWORK_TYPE_GPRS:
                    case NETWORK_TYPE_EDGE:
                    case NETWORK_TYPE_CDMA:
                    case NETWORK_TYPE_1xRTT:
                    case NETWORK_TYPE_IDEN:
                        return Type.MOBILE_2G_NET;
                    case NETWORK_TYPE_UMTS:
                    case NETWORK_TYPE_EVDO_0:
                    case NETWORK_TYPE_EVDO_A:
                    case NETWORK_TYPE_HSDPA:
                    case NETWORK_TYPE_HSUPA:
                    case NETWORK_TYPE_HSPA:
                    case NETWORK_TYPE_EVDO_B:
                    case NETWORK_TYPE_EHRPD:
                    case NETWORK_TYPE_HSPAP:
                        return Type.MOBILE_3G_NET;
                    case NETWORK_TYPE_LTE:
                        return Type.MOBILE_4G_NET;
                    default:
                        return Type.UNKNOWN_NET;
                }
            }
        }
        //无法识别网络
        return Type.UNKNOWN_NET;
    }

    /**
     * 网络类型
     */
    public enum Type {
        INVALID_NET("无效网络"),
        WIFI_NET("Wifi"),
        MOBILE_2G_NET("2G"),
        MOBILE_3G_NET("3G"),
        MOBILE_4G_NET("4G"),
        UNKNOWN_NET("未知网络");
        String desc;

        Type(String desc) {
            this.desc = desc;
        }
    }
}
