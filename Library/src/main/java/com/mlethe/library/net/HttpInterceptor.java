package com.mlethe.library.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 拦截器
 * 向请求头里添加公共参数
 * Created by Mlethe on 2018/2/22.
 */
public class HttpInterceptor implements Interceptor {
    private Map<String, String> mHeaderParamsMap = new HashMap<>();

    public HttpInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
//        Log.e("HttpInterceptor","add common params");
        Request oldRequest = chain.request();
        // 添加新的参数，添加到url 中
        /*HttpUrl.Builder authorizedUrlBuilder = oldRequest.url().newBuilder()
        .scheme(oldRequest.url().scheme())
        .host(oldRequest.url().host());*/

        // 新的请求
        Request.Builder requestBuilder = oldRequest.newBuilder()
                .method(oldRequest.method(), oldRequest.body());

        //添加公共参数,添加到header中
        if (mHeaderParamsMap != null && mHeaderParamsMap.size() > 0) {
            for (Map.Entry<String, String> params : mHeaderParamsMap.entrySet()) {
                requestBuilder.header(params.getKey(), params.getValue());
            }
            mHeaderParamsMap.clear();
        }
        Request newRequest = requestBuilder.build();
        return chain.proceed(newRequest);
    }

    public static class Builder {
        HttpInterceptor mHttpCommonInterceptor;

        public Builder() {
            mHttpCommonInterceptor = new HttpInterceptor();
        }

        public Builder addHeaderParam(String key, String value) {
            mHttpCommonInterceptor.mHeaderParamsMap.put(key, value);
            return this;
        }

        public Builder addHeaderParams(Map<String, String> headers) {
            mHttpCommonInterceptor.mHeaderParamsMap = headers;
            return this;
        }

        public Builder addHeaderParam(String key, int value) {
            return addHeaderParam(key, String.valueOf(value));
        }

        public Builder addHeaderParam(String key, float value) {
            return addHeaderParam(key, String.valueOf(value));
        }

        public Builder addHeaderParam(String key, long value) {
            return addHeaderParam(key, String.valueOf(value));
        }

        public Builder addHeaderParam(String key, double value) {
            return addHeaderParam(key, String.valueOf(value));
        }

        public HttpInterceptor build() {
            return mHttpCommonInterceptor;
        }

    }
}
