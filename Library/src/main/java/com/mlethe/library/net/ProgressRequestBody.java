package com.mlethe.library.net;

import android.os.Handler;
import android.os.Looper;

import com.mlethe.library.net.callback.IProcess;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by Mlethe on 2018/2/22.
 */
class ProgressRequestBody extends RequestBody {

    private RequestBody mRequestBody;
    private final IProcess PROCESS;
    private final Handler mHandler;

    public ProgressRequestBody(RequestBody requestBody, IProcess process) {
        this.mRequestBody = requestBody;
        this.PROCESS = process;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        // 解决进度条调用多次问题
        if (sink instanceof Buffer) {
            // Log Interceptor
            mRequestBody.writeTo(sink);
            return;
        }
        CountingSink countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);
        // 写入
        mRequestBody.writeTo(bufferedSink);
        // 刷新
        // 必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }

    class CountingSink extends ForwardingSink {

        // 当前写入字节数
        private float bytesWritten = 0;

        // 总字节长度，避免多次调用contentLength()方法
        private float totalLength = 0;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            if (totalLength <= 0) {
                totalLength = contentLength();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (PROCESS != null) {
                        PROCESS.onProgress(bytesWritten, totalLength);
                    }
                }
            });
        }
    }
}
