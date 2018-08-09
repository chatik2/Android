package com.shifu.user.truechat;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.shifu.user.truechat.model.Msg;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Collections;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private final static String URL = "http://138.197.146.14/";

    private  static ApiClient instance;
    private  Retrofit retrofit;

    private ApiClient(){
        buildRetrofit();
    }

    public synchronized static ApiClient getInstance(){
        if (instance == null)
            instance = new ApiClient();
        return instance;
    }

    private  Retrofit buildRetrofit(){

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        //log url body
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpBuilder
//                .addInterceptor(loggingInterceptor);
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    okhttp3.Response response = chain.proceed(request);
                    Log.d("Request: ", request.url().toString());
                    // Обработка путого запроса (самостоятельно retrofit обрабатывает некорректно - нужен json

                    if (response.body() != null) {
                        BufferedSource source = response.body().source();
                        source.request(Long.MAX_VALUE); // Buffer the entire body.
                        Buffer buffer = source.buffer();

                        Log.d("Response", "size: " + buffer.size() + "-byte body");

                        if (buffer.size() == 0) {
                            System.out.println("Response body is empty");
                            MediaType contentType = response.body().contentType();
                            ResponseBody body = ResponseBody.create(contentType, "[{}]");
                            response = response.newBuilder().body(body).build();
                        }
                    }
                    return response;
                });

        retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .client(okHttpBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return  retrofit;
    }

    public ApiInterface getApi(){
        return  retrofit.create(ApiInterface.class);
    }
}
