package com.shifu.user.truechat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject implements UserFields, MyRealms{
    private static final String FIELD_ID = "suid";

    @SerializedName("id")
    @Expose
    @PrimaryKey
    private Long suid;

    @SerializedName("name")
    @Expose
    private String name;

    // To get newInstance of class, to use "static" function getIdField
    public User(){}


    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Long getSuid() {
        return suid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("suid", suid)
                .append("name", name)
                .toString();
    }

    @Override
    public String getIdField() {
        return FIELD_ID;
    }
}
