package com.mlethe.rxjavaretrofit.demo;

import android.app.Application;

import com.mlethe.library.app.ProjectInit;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ProjectInit.init(this)
                .withApiHost("http://releases.b0.upaiyun.com/","http://wx.sjhn520.com/")
                .setLogEnable(true)
                .configure();
    }
}
