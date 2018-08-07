package com.shifu.user.truechat.realm;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.concurrent.atomic.AtomicLong;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Msg extends RealmObject {


    private static AtomicLong INTEGER_COUNTER = new AtomicLong(0);
    public static final String FIELD_ID = "mid";

    @PrimaryKey
    private Long mid;

    private Long id;
    private String text;
    private String date;
    private Long uid;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("Local_id", mid)
                .append("Net_id", id)
                .append("text", text)
                .append("date", date)
                .append("uid", uid)
                .toString();
    }

    public static long increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }

    public Long getMid() {
        return mid;
    }

    public Long getid() {
        return id;
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

    public void setUid(Long uid) {
        this.uid = uid;
    }
    public Long getUid(){
        return this.uid;
    }
}

