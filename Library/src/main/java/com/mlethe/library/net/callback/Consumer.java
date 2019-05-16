package com.mlethe.library.net.callback;

import android.content.Intent;
import android.net.Uri;

import com.mlethe.library.app.ProjectInit;
import com.mlethe.library.utils.file.FileUtil;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

/**
 * Created by Mlethe on 2018/6/13.
 */
public abstract class Consumer<T> implements Observer<T> {

    @Override
    public final void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
        if (t instanceof File) {
            //下到了APK直接安装
            autoInstallApk((File) t);
        }
    }

    @Override
    public final void onError(Throwable throwable) {
        if (throwable instanceof HttpException) {//请求的地址不存在
            int code = ((HttpException) throwable).code();
            onError(code, throwable.getMessage());
        } else {
            onFailure(throwable);
        }
    }

    @Override
    public final void onComplete() {
        onRequestEnd();
    }

    /**
     * 请求成功
     *
     * @param result
     */
    protected abstract void onSuccess(T result);

    /**
     * 请求错误
     * @param code
     * @param msg
     */
    protected void onError(int code, String msg){}

    /**
     * 请求结束
     */
    protected void onRequestEnd(){}

    /**
     * 请求异常
     *
     * @param throwable
     */
    protected abstract void onFailure(Throwable throwable);

    private void autoInstallApk(File file) {
        if (FileUtil.getExtension(file.getPath()).equals("apk")) {
            final Intent install = new Intent();
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.setAction(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            ProjectInit.getApplicationContext().startActivity(install);
        }
    }
}
