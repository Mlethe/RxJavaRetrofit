package com.mlethe.library.net;

import com.mlethe.library.net.callback.IProcess;
import com.mlethe.library.net.callback.IRequest;
import com.mlethe.library.net.download.DownloadHandler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

public class RestClient {

    private HashMap<String, String> mHeaders = new HashMap<>();
    private HashMap<String, Object> mParams = new HashMap<>();
    private String mUrl;

    private String mDownloadDir;
    private String mExtension;
    private String mFilename;

    private IRequest mIRequest;
    private IProcess mIProcess;

    private RestClient() {
    }

    public static final RestClient getInstance() {
        return new RestClient();
    }

    /**
     * 添加请求开始回调
     * @param request
     * @return
     */
    public RestClient request(IRequest request) {
        mIRequest = request;
        return this;
    }

    /**
     * 添加进度回调
     * @param process
     * @return
     */
    public RestClient process(IProcess process) {
        mIProcess = process;
        return this;
    }

    /**
     * 添加头部参数
     * @param key
     * @param value
     * @return
     */
    public RestClient addHeader(String key, Object value) {
        mHeaders.put(key, String.valueOf(value));
        return this;
    }

    /**
     * 添加头部参数
     * @param headers
     * @return
     */
    public RestClient addHeaders(Map<String, String> headers) {
        mHeaders.putAll(headers);
        return this;
    }

    /**
     * 获取Retrofit对象
     * @param clazz
     * @param <T>
     * @return
     */
    public final <T> T create(Class<T> clazz) {
        RestCreator.getInstance().addHeader(mHeaders);
        if (mIRequest != null) {
            mIRequest.onRequestStart();
        }
        return RestCreator.getInstance().create(clazz);
    }

    /**
     * 下载的请求URL
     * @param url
     * @return
     */
    public final RestClient url(String url) {
        this.mUrl = url;
        return this;
    }

    /**
     * 下载保存的文件目录
     * @param dir 文件目录
     * @return
     */
    public final RestClient dir(String dir) {
        this.mDownloadDir = dir;
        return this;
    }

    /**
     * 下载保存的文件后缀
     * @param extension
     * @return
     */
    public final RestClient extension(String extension) {
        this.mExtension = extension;
        return this;
    }

    /**
     * 下载保存的文件
     * @param filename 文件名
     * @return
     */
    public final RestClient filename(String filename) {
        this.mFilename = filename;
        return this;
    }

    /**
     * 下载
     * @return
     */
    public final Observable<File> download() {
        return new DownloadHandler(mParams, mHeaders, mUrl, mIRequest, mIProcess, mDownloadDir, mExtension, mFilename).handleDownload();
    }
}
