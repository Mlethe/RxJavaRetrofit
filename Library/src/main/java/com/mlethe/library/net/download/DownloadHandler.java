package com.mlethe.library.net.download;

import com.mlethe.library.net.RestCreator;
import com.mlethe.library.net.callback.IProcess;
import com.mlethe.library.net.callback.IRequest;
import com.mlethe.library.utils.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by Mlethe on 2018/6/6.
 */

public class DownloadHandler {
    private final HashMap<String, Object> PARAMS;
    private final HashMap<String, String> HEADERS;
    private final String URL;
    private final IRequest REQUEST;
    private final IProcess PROCESS;
    private final String DOWNLOAD_DIR;
    private final String EXTENSION;
    private final String FILENAME;

    public DownloadHandler(HashMap<String, Object> params,
                           HashMap<String, String> headers,
                           String url, IRequest request, IProcess process,
                           String downloadDir, String extension, String filename) {
        this.PARAMS = params;
        this.HEADERS = headers;
        this.URL = url;
        this.REQUEST = request;
        this.PROCESS = process;
        this.DOWNLOAD_DIR = downloadDir;
        this.EXTENSION = extension;
        this.FILENAME = filename;
    }

    public final Observable<File> handleDownload() {
        RestCreator.getInstance().addHeader(HEADERS);
        if (REQUEST != null) {
            REQUEST.onRequestStart();
        }
        return RestCreator.getInstance().getRestService()
                .download(URL, PARAMS)
                /*生命周期管理*/
                .subscribeOn(Schedulers.io())
                .map(new Function<ResponseBody, File>() {
                    @Override
                    public File apply(ResponseBody body) throws Exception {
                        String downloadDir = DOWNLOAD_DIR;
                        if (DOWNLOAD_DIR == null || DOWNLOAD_DIR.equals("")) {
                            downloadDir = "down_loads";
                        }
                        String extension = EXTENSION;
                        if (EXTENSION == null) {
                            extension = "";
                        }
                        long totalLength = body.contentLength();
                        File file;
                        String name = FILENAME;
                        if (FILENAME == null) {
                            file =  FileUtil.createFileByTime(downloadDir, extension.toUpperCase(), extension);
                        } else {
                            file =  FileUtil.createFile(downloadDir, name);
                        }
                        BufferedSink mSink = Okio.buffer(Okio.sink(file));
                        mSink.writeAll(Okio.buffer(getProgressSource(body.source(), totalLength)));
                        mSink.close();
                        return file;
                    }
                }).unsubscribeOn(Schedulers.io());
    }

    private Source getProgressSource(BufferedSource source, final float totalLength) {
        return new ForwardingSource(source) {
            private long mDownLoadLength = 0;   // 已经下载的大小
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long read = super.read(sink, byteCount);

                mDownLoadLength += read == -1 ? 0 : read;
                if (totalLength <= 0) {
                    if (PROCESS != null) {
                        PROCESS.onProgress(0);
                    }
                } else {
                    float percent = mDownLoadLength / totalLength;
                    if (PROCESS != null) {
                        PROCESS.onProgress(percent);
                    }
                }
                return read;
            }
        };
    }



}
