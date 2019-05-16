package com.mlethe.demo;

import io.reactivex.Observable;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ServerApi {

    @POST("")
    @FormUrlEncoded
    Observable<Result> add();

}
