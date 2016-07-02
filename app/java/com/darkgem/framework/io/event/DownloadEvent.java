package com.darkgem.framework.io.event;

/**
 * 下载完文件通知
 */
public class DownloadEvent {
    long id;
    String fileName;

    public DownloadEvent(long id, String fileName) {
        this.id = id;
        this.fileName = fileName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
