package com.mlethe.library.net;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.mlethe.library.app.ProjectInit;
import com.mlethe.library.net.callback.IProcess;
import com.mlethe.library.net.callback.IRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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
class DownloadHandler {
    private final HashMap<String, Object> PARAMS;
    private final HashMap<String, String> HEADERS;
    private final String URL;
    private final IRequest REQUEST;
    private final IProcess PROCESS;
    private final String DOWNLOAD_DIR;  // 文件夹名称
    private final String EXTENSION; // 后缀名
    private final String FILENAME;  // 文件名
    private final Handler mHandler;

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
        this.mHandler = new Handler(Looper.getMainLooper());
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
                            downloadDir = "Downloads";
                        }
                        String extension = EXTENSION;
                        if (EXTENSION == null) {
                            extension = "";
                        }
                        long totalLength = body.contentLength();
                        File file;
                        if (FILENAME == null) {
                            file = createFileByTime(downloadDir, extension.toUpperCase(), extension);
                        } else {
                            file = createFile(downloadDir, FILENAME);
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
            private float mDownLoadLength = 0;   // 已经下载的大小

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long read = super.read(sink, byteCount);

                mDownLoadLength += read == -1 ? 0 : read;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (PROCESS != null) {
                            PROCESS.onProgress(mDownLoadLength, totalLength);
                        }
                    }
                });
                return read;
            }
        };
    }

    /**
     * 根据时间创建文件
     *
     * @param sdcardDirName    文件夹名称
     * @param timeFormatHeader 时间文件名前缀
     * @param extension        文件后缀名
     * @return
     */
    private static File createFileByTime(String sdcardDirName, String timeFormatHeader, String extension) {
        final Date date = new Date(System.currentTimeMillis());
        // 必须要加上单引号
        SimpleDateFormat dateFormat = new SimpleDateFormat("'" + timeFormatHeader + "'_yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = dateFormat.format(date) + "." + extension;
        return createFile(sdcardDirName, fileName);
    }

    /**
     * 创建文件
     *
     * @param sdcardDirName 文件夹名称
     * @param fileName      文件名
     * @return
     */
    private static File createFile(String sdcardDirName, String fileName) {
        return new File(createDir(sdcardDirName), fileName);
    }

    /**
     * 创建下载目录
     *
     * @param sdcardDirName 文件夹名称
     * @return
     */
    private static String createDir(String sdcardDirName) {
        String apkPath = getCacheDirectory(ProjectInit.getApplicationContext()) + File.separator + sdcardDirName;
        final File fileDir = new File(apkPath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir.getPath();
    }

    /**
     * 创建缓存（Android/data/包名/cache目录下）
     *
     * @return
     */
    private static String getCacheDirectory(Context context) {
        String cachePath = null;
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            return cachePath;
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null) return cachePath;
        cachePath = externalCacheDir.getPath();
        if (cachePath != null) return cachePath;
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && cacheDir.exists()) {
            cachePath = cacheDir.getPath();
        }
        return cachePath;
    }

}
