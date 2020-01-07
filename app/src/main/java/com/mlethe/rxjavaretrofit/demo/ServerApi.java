package com.mlethe.rxjavaretrofit.demo;

import com.mlethe.library.net.RestClient;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface ServerApi {

    @POST("test/add")
    @FormUrlEncoded
    Observable<Result> add(@Field("name") String name);

    /**
     * 单上传文件
     * @param parts
     * @return
     */
    @Headers({RestClient.BASE_URL_HEADER + ":2"})
    @Multipart
    @POST("test/uploads")
    Observable<Result> upload(@Part MultipartBody.Part parts, @PartMap Map<String, RequestBody> params);

    /**
     * 多上传文件  参数名称一致（例：file[]）
     * @param parts
     * @return
     */
    @Headers({RestClient.BASE_URL_HEADER + ":2"})
    @Multipart
    @POST("test/uploads")
    Observable<Result> upload(@Part List<MultipartBody.Part> parts, @PartMap Map<String, RequestBody> params);

}
