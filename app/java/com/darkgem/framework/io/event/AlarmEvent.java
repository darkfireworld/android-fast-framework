package com.darkgem.framework.io.event;

/**
 * 闹钟事件，每隔5分钟出现一次
 */
public class AlarmEvent {
    //当前发送了多少次闹钟事件
    long count;

    public AlarmEvent(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }
}
