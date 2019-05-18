package com.mlethe.demo;

import android.os.Bundle;
import android.widget.Toast;

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
                    public void onSuccess(Result result) {
                        if (result.isOk()) {
                            Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Toast.makeText(MainActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
