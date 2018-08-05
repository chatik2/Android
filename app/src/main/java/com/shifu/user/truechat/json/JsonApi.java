package com.shifu.user.truechat.json;

import java.util.List;

import io.reactivex.Flowable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JsonApi  {

    @GET("/new_user")
    Flowable<Response<JsonUid>> getUid();

    @GET("/users")
    Flowable<Response<List<JsonUser>>> getUsers(@Query("uid") Long uid);

    @GET("/msgs")
    Flowable<Response<List<JsonMsg>>> getMsgs(@Query("uid") Long uid);

    @GET(".")
    Flowable<Response<JsonMsg>> pushMsg(@Query("uid") Long uid, @Query("text") String text);

//    @GET("{table}")
//    Flowable<Response<List<T>>> get(@Path("table") String table, @Query("uid") Long uid);

}
