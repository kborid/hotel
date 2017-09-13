package com.prj.sdk.net.down;


public interface DownCallback {

    void beginDownload(String url, String local, String fileName, int status);

    void downloading(int status, int progress, int maxLength);

    void finishDownload(int status);
}