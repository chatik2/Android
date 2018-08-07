package com.shifu.user.truechat.realm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Author extends RealmObject {
    public static final String FIELD_ID = "uid";

    @PrimaryKey
    @SerializedName("id")
    @Expose
    private Long uid;

    @SerializedName("name")
    @Expose
    private String username;

    public String getUsername() {
        return username;
    }

    public Long getUid() {
        return uid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uid", uid)
                .append("username", username)
                .toString();
    }
}
