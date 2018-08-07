package com.shifu.user.truechat.json;

import com.shifu.user.truechat.realm.Author;
import com.shifu.user.truechat.realm.User;

import java.util.List;

import io.reactivex.Flowable;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("/new_user")
    Flowable<Response<Author>> getUid();

    @GET("/users")
    Flowable<Response<List<User>>> getUsers(@Header("uid") Long uid);

    @GET("/msgs")
    Flowable<Response<List<JsonMsg>>> getMsgs(@Header("uid") Long uid);

    @POST("/new_msg")
    Flowable<Response<JsonMsg>> newMsg(@Query("uid") Long uid, @Query("text") String text);

    @POST("/new_name")
    Flowable<Response<Author>> pushName(@Header("uid") Long uid, @Body String name);

    @POST("/new_msg")
    Flowable<Response<Author>> pushMsg(@Query("uid") Long uid, @Body RequestBody name);

}
