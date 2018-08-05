package com.shifu.user.truechat.realm;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.concurrent.atomic.AtomicLong;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Msgs extends RealmObject {


    private static AtomicLong INTEGER_COUNTER = new AtomicLong(0);
    public static final String FIELD_ID = "mid";

    @PrimaryKey
    private Long mid;

    private Long uid;
    private String text;
    private String date;

    public Long getMid() {
        return mid;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getText() {
        return text;
    }
    public void setText(String data) {
        this.text = data;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String data) {
        this.date = data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mid", mid)
                .append("uid", uid)
                .append("text", text)
                .append("date", date)
                .toString();
    }

    public static long increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }
}

