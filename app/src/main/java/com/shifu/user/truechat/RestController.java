package com.shifu.user.truechat;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.shifu.user.truechat.json.JsonMsg;
import com.shifu.user.truechat.json.JsonUser;
import com.shifu.user.truechat.json.JsonApi;
import com.shifu.user.truechat.json.JsonUid;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import okhttp3.OkHttpClient;

import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.shifu.user.truechat.realm.Author;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;


public class RestController {

    private final static String CLASS_TAG = "REST.";
    private final static String URL = "http://138.197.146.14/";
    private final static int timeout = 10; // в секундах

    private final static RealmController rc;

    // Предполагается, что вызов после MainActivity onCreate
    static {
        rc = RealmController.getInstance();
    }

    static JsonApi init() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        return retrofit.create(JsonApi.class);


    }

    public static Flowable<Response<JsonUid>> login() {
         return init().getUid();
    }

    public static Flowable<Response<List<JsonUser>>> getUsers(Long uid) {
        return init().getUsers(uid);
    }

    public static Flowable<Response<List<JsonMsg>>> getMsgs(Long uid) {
        return init().getMsgs(uid);
    }

    static void handleError(Throwable t, String tag) {
        String TAG = CLASS_TAG+tag;
        Log.e(TAG, "Failure: " + t.toString());
    }

    private  static void ResponseError(String TAG, String errorMessage, Handler h) throws JSONException {
            String error = new JSONObject(errorMessage).getJSONObject("error").getString("message");
            Log.e(TAG+"Error: ", error);
            h.sendMessage(Message.obtain(h, 0, error));
    }

}
