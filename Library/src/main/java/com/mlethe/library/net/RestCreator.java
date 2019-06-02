package com.mlethe.library.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.mlethe.library.app.ConfigKeys;
import com.mlethe.library.app.ProjectInit;
import com.mlethe.library.net.callback.IProcess;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Mlethe on 2018/6/6.
 */
final class RestCreator {

    private static final String TAG = "RestCreator";

    private static final boolean LOG_ENABLE = ProjectInit.getConfiguration(ConfigKeys.LOG_ENABLE);

    protected static final String BASE_URL_HEADER = "base_url_order";

    private static final Map<String, String> HEADERS = new HashMap<>();

    private static final BlockingQueue<IProcess> mIProcessPool = new LinkedBlockingQueue<>();

    private RestCreator() {
    }

    /**
     * 产生一个全局的Retrofit客户端
     */
    private static final class RetrofitHolder {
        private static final String[] BASE_URLS = ProjectInit.getConfiguration(ConfigKeys.API_HOSTS);
        private static final Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
                .baseUrl(BASE_URLS == null ? null : BASE_URLS[0])
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())  // RxJava转换器
                .client(OKHttpHolder.OK_HTTP_CLIENT)
                .build();
    }

    private static final class OKHttpHolder {
        private static final int DEFAULT_READ_TIME_OUT = 30;    // 读写超时时间30秒
        private static final long DEFAULT_DIR_CACHE = 1024 * 1024 * 10; // 缓存大小10M
        private static final int TIMEOUT_CONNECT = 5; // 5秒
        private static final int TIMEOUT_DISCONNECT = 60 * 60 * 24 * 7; // 7天
        private static final int TIME_OUT = 60;   // 超时
        // google建议放到这里(OkHttp缓存只支持GET请求)
        private static final File cacheFile = new File(ProjectInit.getApplicationContext().getCacheDir(), "caheData");
        // 设置缓存大小
        private static final Cache cache = new Cache(cacheFile, DEFAULT_DIR_CACHE);

        /**
         * 在线缓存
         */
        private static final Interceptor REWRITE_RESPONSE_INTERCEPTOR = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //获取retrofit @headers里面的参数，参数可以自己定义，在本例我自己定义的是cache，跟@headers里面对应就可以了
                String cache = chain.request().header("cache");
                if (LOG_ENABLE) {
                    Log.e(TAG, "intercept: cache->" + cache);
                }
                Response originalResponse = chain.proceed(chain.request());
                String cacheControl = originalResponse.header("Cache-Control");
                //如果cacheControl为空，就让他TIMEOUT_CONNECT秒的缓存，本例是5秒，方便观察。注意这里的cacheControl是服务器返回的
                if (cacheControl == null) {
                    //如果cache没值，缓存时间为TIMEOUT_CONNECT，有的话就为cache的值
                    if (cache == null || "".equals(cache)) {
                        cache = TIMEOUT_CONNECT + "";
                    }
                    originalResponse = originalResponse.newBuilder()
                            .header("Cache-Control", "public, max-age=" + cache)
                            .build();
                    return originalResponse;
                } else {
                    return originalResponse;
                }
            }
        };

        /**
         * 检测当前网络是否可用
         *
         * @return
         */
        private static final boolean isNetworkConnected() {
            // 得到连接管理器对象
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) ProjectInit.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        /**
         * 离线缓存
         */
        private static final Interceptor REWRITE_RESPONSE_INTERCEPTOR_OFFLINE = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                //离线的时候为7天的缓存。
                if (!isNetworkConnected()) {
                    request = request.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + TIMEOUT_DISCONNECT)
                            .build();
                }
                return chain.proceed(request);
            }
        };

        // Header公共参数拦截器
        private static final HttpInterceptor HEADER_INTERCEPTOR = new HttpInterceptor.Builder()
                .addHeaderParams(HEADERS)
                .build();

        // 多baseUrl切换
        private static final Interceptor CHANGE_BASE_URL_INTERCEPTOR = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // 获取request
                Request request = chain.request();
                // 获取request的创建者builder
                Request.Builder builder = request.newBuilder();
                // 从request中获取headers，通过给定的键url_name
                List<String> headerValues = request.headers(BASE_URL_HEADER);
                int urlLength = RetrofitHolder.BASE_URLS.length;
                if (headerValues != null && headerValues.size() > 0 && urlLength > 1) {
                    // 如果有这个header，先将配置的header删除，因此header仅用作app和okhttp之间使用
                    builder.removeHeader(BASE_URL_HEADER);

                    // 匹配获得新的BaseUrl
                    int headerValue = Integer.parseInt(headerValues.get(0));
                    if (headerValue >= 2) {
                        HttpUrl newBaseUrl = HttpUrl.parse(RetrofitHolder.BASE_URLS[headerValue - 1]);
                        // 从request中获取原有的HttpUrl实例oldHttpUrl
                        HttpUrl oldHttpUrl = request.url();
                        // 重建新的HttpUrl，修改需要修改的url部分
                        HttpUrl newFullUrl = oldHttpUrl
                                .newBuilder()
                                .scheme(newBaseUrl.scheme())
                                .host(newBaseUrl.host())
                                .port(newBaseUrl.port())
                                .build();
                        // 重建这个request，通过builder.url(newFullUrl).build()；
                        // 然后返回一个response至此结束修改
                        return chain.proceed(builder.url(newFullUrl).build());
                    }
                }
                return chain.proceed(request);
            }
        };

        // 上传进度条拦截器
        private static final Interceptor UPLOAD_PROCESS_INTERCEPTOR = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // 获取request
                Request request = chain.request();
                // 获取request的创建者builder
                Request.Builder builder = request.newBuilder();
                RequestBody requestBody = request.body();
                if (requestBody instanceof MultipartBody) {
                    try {
                        IProcess iProcess = mIProcessPool.poll(5, TimeUnit.SECONDS);
                        if (iProcess == null) return chain.proceed(request);
                        return chain.proceed(builder.method(request.method(), new ProgressRequestBody(requestBody, iProcess)).build());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return chain.proceed(request);
                    }
                }
                return chain.proceed(request);
            }
        };

        private static final List<Interceptor> INTERCEPTORS = ProjectInit.getConfiguration(ConfigKeys.INTERCEPTORS);

        private static final OkHttpClient.Builder BUILDER = new OkHttpClient.Builder()
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS)  //写操作超时时间
                .readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS)   //读操作超时时间
                .addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR)    //有网络时的拦截器
                .addInterceptor(REWRITE_RESPONSE_INTERCEPTOR_OFFLINE)   // 没网络时的拦截器
                .cache(cache)
                .addInterceptor(HEADER_INTERCEPTOR)
                .addInterceptor(CHANGE_BASE_URL_INTERCEPTOR)
                .addInterceptor(UPLOAD_PROCESS_INTERCEPTOR);

        static {
            BUILDER.interceptors().addAll(INTERCEPTORS);
            if (LOG_ENABLE) {
                BUILDER.addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        // 打印返回结果信息
                        Log.e(TAG, message);
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY));
            }
        }

        private static final OkHttpClient OK_HTTP_CLIENT = BUILDER.build();

    }

    //提供接口让调用者得到RestService对象
    private static final class RestServiceHolder {
        private static final RestService REST_SERVICE = RetrofitHolder.RETROFIT_CLIENT.create(RestService.class);
    }

    //提供接口让调用者得到RestCreator对象
    private static final class RestCreatorHolder {
        private static final RestCreator REST_CREATOR = new RestCreator();
    }

    protected static final RestCreator getInstance() {
        return RestCreatorHolder.REST_CREATOR;
    }

    protected final <T> T create(Class<T> clazz) {
        return RetrofitHolder.RETROFIT_CLIENT.create(clazz);
    }

    protected final void addHeader(Map<String, String> headers) {
        if (headers != null) {
            HEADERS.putAll(headers);
        }
    }

    /**
     * 获取RestService对象
     * @return
     */
    protected final RestService getRestService() {
        return RestServiceHolder.REST_SERVICE;
    }

    /**
     * 设置上传进度条
     * @param process
     * @return
     */
    protected final RestCreator setProcess(IProcess process) {
        if (process != null)
            mIProcessPool.add(process);
        return this;
    }

}








