package com.mlethe.rxjavaretrofit.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mlethe.library.net.RestClient;
import com.mlethe.library.net.UploadUtil;
import com.mlethe.library.net.callback.Consumer;
import com.mlethe.library.net.callback.IRequest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private ProgressBar downloadPb, uploadOnePb, uploadMorePb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.download_btn).setOnClickListener(this);
        findViewById(R.id.upload_one_btn).setOnClickListener(this);
        findViewById(R.id.upload_more_btn).setOnClickListener(this);
        downloadPb = findViewById(R.id.download_pb);
        uploadOnePb = findViewById(R.id.upload_one_pb);
        uploadMorePb = findViewById(R.id.upload_more_pb);
        /*RestClient.getInstance()
                .create(ServerApi.class)
                .add("test")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                });*/
    }

    private void autoInstallApk(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        } else {
            // 声明需要的临时权限
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // 第二个参数，即第一步中配置的authorities
            Uri contentUri = FileProvider.getUriForFile(this, getApplicationInfo().packageName + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.download_btn) {  // 下载
            downloadPb.setProgress(0);
            RestClient.getInstance()
                    .url("hoolay.apk")
                    .filename("hoolay_1.0.2.apk")
                    .process((currentLength, totalLength) -> {
                        downloadPb.setProgress((int) (currentLength * 100 / totalLength));
                        Log.e(TAG, "onProgress: Thread->" + Thread.currentThread().getName() + "    percent->" + (currentLength / totalLength));
                    })
                    .download()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<File>() {
                        @Override
                        public void onSuccess(File file) {
                            Log.e(TAG, "onSuccess: Thread->" + Thread.currentThread().getName() + "      path->" + file.getPath());
                            Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                            autoInstallApk(file);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
        } else if (id == R.id.upload_one_btn) { // 单文件上传
            uploadOnePb.setProgress(0);
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera/VID_20180322_210509.mp4";
            File file = new File(path);
            if (!file.exists()) {
                Log.e(TAG, "upload_one_btn: 文件不存在    path->" + path);
                Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, RequestBody> params = new HashMap<>();
            params.put("userId", UploadUtil.createRequestBodyOfText("2"));
            RestClient.getInstance()
                    .process((currentLength, totalLength) -> {
                        uploadOnePb.setProgress((int) (currentLength * 100 / totalLength));
                        Log.e(TAG, "onProgress: Thread->" + Thread.currentThread().getName() + "    percent->" + (currentLength / totalLength));
                    })
                    .create(ServerApi.class)
                    .upload(UploadUtil.createMultipartBodyPartOfForm("file", file), params)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Result>() {
                        @Override
                        public void onSuccess(Result result) {
                            Log.e(TAG, "onSuccess: " + result.toString());
                            Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
        } else if (id == R.id.upload_more_btn) { // 多文件上传
            uploadMorePb.setProgress(0);
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera/VID_20180322_210509.mp4";
            String path2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera/IMG_20190602_141950.jpg";
            File file = new File(path);
            File file2 = new File(path2);
            if (!file.exists() && !file2.exists()) {
                Log.e(TAG, "upload_more_btn: 文件不存在    path->" + path);
                Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String, RequestBody> params = new HashMap<>();
            params.put("userId", UploadUtil.createRequestBodyOfText("2"));
            RestClient.getInstance()
                    .process((currentLength, totalLength) -> {
                        uploadMorePb.setProgress((int) (currentLength * 100 / totalLength));
                        Log.e(TAG, "onProgress: Thread->" + Thread.currentThread().getName() + "    percent->" + (currentLength / totalLength));
                    })
                    .create(ServerApi.class)
                    .upload(UploadUtil.createMultipartBodyPartsOfForm("file[]", file, file2), params)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Result>() {
                        @Override
                        public void onSuccess(Result result) {
                            Log.e(TAG, "onSuccess: " + result.toString());
                            Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
        }
    }
}
