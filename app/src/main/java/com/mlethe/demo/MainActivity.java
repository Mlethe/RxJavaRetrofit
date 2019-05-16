package com.mlethe.demo;

import android.os.Bundle;

import com.mlethe.library.net.RestClient;
import com.mlethe.library.net.callback.Consumer;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RestClient.getInstance().create(ServerApi.class)
                .add()
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Result>() {
                    @Override
                    protected void onSuccess(Result result) {

                    }

                    @Override
                    protected void onFailure(Throwable throwable) {

                    }
                });
    }
}
