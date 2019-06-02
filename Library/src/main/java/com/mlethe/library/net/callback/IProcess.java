package com.mlethe.library.net.callback;

/**
 * Created by Mlethe on 2018/6/6.
 */
public interface IProcess {
    /**
     * 下载进度
     * @param currentLength 当前长度
     * @param totalLength 总长度
     */
    void onProgress(float currentLength, float totalLength);
}
