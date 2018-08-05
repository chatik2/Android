package com.shifu.user.truechat;

import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shifu.user.truechat.json.JsonMsg;
import com.shifu.user.truechat.json.JsonUid;
import com.shifu.user.truechat.json.JsonUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import retrofit2.Response;

public class FakeRestController {

    private final static String TAG = FakeRestController.class.getSimpleName();

    private static String getJson(InputStream inputStream) {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int pointer;
            while ((pointer = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, pointer);
            }
        } catch (IOException exception) {
            Log.e(TAG, "Error writing/reading from the JSON file.", exception);
        } finally {
            try {
                inputStream.close();
            } catch (IOException exception) {
                Log.e(TAG, "Error closing the input stream.", exception);
            }
        }
        return writer.toString();
    }

    public static Flowable<Response<List<JsonUser>>> getUsers(InputStream inputStream, Long uid) {
        Gson gson = new Gson();
        Type ListType = new TypeToken<ArrayList<JsonUser>>(){}.getType();
        List<JsonUser> list = gson.fromJson(getJson(inputStream), ListType);
        return Flowable.just(Response.success(list));
    }

    public static Flowable<Response<List<JsonMsg>>> getMsgs(InputStream inputStream, Long uid) {
        Gson gson = new Gson();
        Type ListType = new TypeToken<ArrayList<JsonMsg>>(){}.getType();
        List<JsonMsg> list = gson.fromJson(getJson(inputStream), ListType);
        return Flowable.just(Response.success(list));
    }

    static void handleError(Throwable t, String tag) {
        Log.e(TAG, "Failure: " + t.toString());
    }

}
