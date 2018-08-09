package com.shifu.user.truechat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Msg extends RealmObject {
    public static final String FIELD_ID = "id";

//    @PrimaryKey
//    @SerializedName("id")
    @Expose
    private Long id;

    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("date")
    @Expose
    private String date;

    @SerializedName("secondUserId")
    @Expose
    private Long uid;

    @SerializedName("lastActivityDate")
    @Expose
    private String lastActivityDate;

    @SerializedName("lastRequestDate")
    @Expose
    private String lastRequestDate;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("local id", id)
                .append("text", text)
                .append("date", date)
                .append("uid", uid)
                .toString();
    }


    public Long getId() {
        return id;
    }

    // In transaction only
    public Long setId(Long uuid) {
        this.id = uuid;
        return uuid;
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

