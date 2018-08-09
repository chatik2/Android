package com.shifu.user.truechat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject{
    public static final String FIELD_ID = "id";

    @SerializedName("id")
    @Expose
    @PrimaryKey
    private Long id;

    @SerializedName("name")
    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .toString();
    }
}
