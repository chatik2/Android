package com.shifu.user.truechat;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.shifu.user.truechat.model.Msg;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private final static String URL = "http://138.197.162.167/";

    private  static ApiClient instance;
    private  static int timeout = 3;

    private  Retrofit retrofit;

     ApiClient(){
        buildRetrofit();
    }

    public synchronized static ApiClient getInstance(int newTimeout){
        if (instance == null || timeout != newTimeout)
            instance = new ApiClient();
        return instance;
    }

    private  Retrofit buildRetrofit(){

        Gson gson = new GsonBuilder()
                .setDateFormat(ListFragment.strDateFormat)
                .setLenient()
                .create();

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder
                .addInterceptor(new customInterceptor())
                .readTimeout(timeout, TimeUnit.SECONDS)
                .connectTimeout(timeout, TimeUnit.SECONDS);

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

    class customInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            try {
                Request request = chain.request();
                Response response = chain.proceed(request);
                Log.d("Request: ", request.url().toString());
                // Обработка путого запроса (самостоятельно retrofit обрабатывает некорректно - нужен json

                if (response.body() != null) {
                    BufferedSource source = response.body().source();
                    source.request(Long.MAX_VALUE); // Buffer the entire body.
                    Buffer buffer = source.buffer();

                    Log.d("Response", "size: " + buffer.size() + "-byte body");

                    String content;
                    MediaType contentType = MediaType.parse("application/json;");
                    ResponseBody body;
                    if (buffer.size() == 0) {
                        System.out.println("Response body is empty");
                        content = "[{}]";
                        body = ResponseBody.create(contentType, content);
                    } else {
                        body = ResponseBody.create(contentType, response.body().bytes());
                    }
                    response = response.newBuilder().body(body).build();
                }
                return response;
            } catch (Exception e) {
                onRestError(e);
                ResponseBody body = ResponseBody.create(MediaType.parse("application/json;"), "[{}]");
                return new Response.Builder()
                        .code(520).message("Нет ответа от сервера")
                        .body(body)
                        .protocol(Protocol.HTTP_1_0)
                        .request(chain.request())
                        .build();
            }
        }
    }

    private static void onRestError(Throwable e) {
        String TAG = "onRestError";
        if (e instanceof HttpException) {
            ResponseBody responseBody = ((HttpException)e).response().errorBody();
            Log.d(TAG, getErrorMessage(responseBody));
        } else if (e instanceof SocketTimeoutException) {
            Log.d(TAG, "SocketTimeoutException");
        } else if (e instanceof IOException) {
            Log.d(TAG, "IOException");
        } else {
            Log.d(TAG, e.getMessage());
        }
    }

    private static String getErrorMessage(ResponseBody responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            return jsonObject.getString("message");
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
