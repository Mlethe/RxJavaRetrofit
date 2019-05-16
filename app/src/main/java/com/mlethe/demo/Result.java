package com.mlethe.demo;

import android.text.TextUtils;

public class Result<T> {
    private String status;
    private String msg;
    private T data;

    public boolean isOk(){
        if (!TextUtils.isEmpty(status) && "success".equals(status)) {
            return true;
        }
        return false;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
