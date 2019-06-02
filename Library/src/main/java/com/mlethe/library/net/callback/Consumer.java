package com.mlethe.library.net.callback;

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
     * @param t
     */
    public abstract void onSuccess(T t);

    /**
     * 请求错误
     * @param code
     * @param msg
     */
    public void onError(int code, String msg){}

    /**
     * 请求结束
     */
    public void onRequestEnd(){}

    /**
     * 请求异常
     *
     * @param throwable
     */
    public abstract void onFailure(Throwable throwable);

}
