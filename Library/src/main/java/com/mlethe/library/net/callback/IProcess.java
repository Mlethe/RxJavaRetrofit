package com.mlethe.library.net.callback;

public interface IProcess {
    /**
     * 下载进度
     * @param percent
     */
    void onProgress(float percent);
}
