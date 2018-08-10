package com.shifu.user.truechat;

import com.shifu.user.truechat.model.Author;
import com.shifu.user.truechat.model.Msg;
import com.shifu.user.truechat.model.User;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiInterface {

    String type = "mobile";

//myTest - all msgs from user creation
//    String type = "web";

    @GET("/new_user")
    Flowable<Response<Author>> getUid();

    @GET("/users")
    Flowable<Response<List<User>>> getUsers(@Header("type") String responseType, @Header("uid") Long uid);

    @GET("/msgs")
    Flowable<Response<List<Msg>>> getMsgs(@Header("type") String responseType,  @Header("uid") Long uid);

    @POST("/new_name")
    Single<Response<Author>> pushName(@Header("uid") Long uid, @Body RequestBody name);

    @POST("/new_msg")
    Single<Response<Msg>> pushMsg(@Header("uid") Long uid, @Body RequestBody text);

}
