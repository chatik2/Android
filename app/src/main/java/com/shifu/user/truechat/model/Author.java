package com.shifu.user.truechat.model;

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

    @SerializedName("secondId")
    @Expose
    private Long id;

    @SerializedName("name")
    @Expose
    private String username;

    // To test
    public Author() {}

    public Author(Long uid, Long id, String username){
        this.uid = uid;
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name){
        this.username = name;
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
