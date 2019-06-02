# Retrofit+Rxjava+okhttp封装

## 依赖工程

* 1.moudel导入工程

```java
  implementation 'com.github.Mlethe:RxJavaRetrofit:1.0.1'
```

* 2.初始化设置：Application中初始化

```java
    ProjectInit.init(this)
        .withApiHost("http://releases.b0.upaiyun.com/","Base_url")
        .setLogEnable(true)   // 开启日志
        .configure();
```

## 代码使用

* 1.普通请求

```java
    RestClient.getInstance()
        .create(ServerApi.class)
        .add("test")
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
```

* 2.上传文件（带进度条）

```java
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
```

* 3.下载文件（带进度条）

```java
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
```
