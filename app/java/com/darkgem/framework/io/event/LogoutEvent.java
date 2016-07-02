package com.darkgem.framework.io.event;

/**
 * 推出系统的类型
 */
public enum LogoutEvent {
    //正常退出
    NORMAL,
    //TOKEN失效
    TOKEN_INVALID,
    //需要强制更新
    FORCE_UPGRADE,
    //PUSH服务器无法连接
    PUSH_CONNECT_INVALID
}
