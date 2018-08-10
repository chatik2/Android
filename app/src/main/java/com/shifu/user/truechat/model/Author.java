package com.shifu.user.truechat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Author extends RealmObject implements UserFields, MyRealms{

    private static final String FIELD_ID = "uuid";

    @SerializedName("id")
    @Expose
    @PrimaryKey
    private Long uuid;

    @SerializedName("secondId")
    @Expose
    private Long suid;

    @SerializedName("name")
    @Expose
    private String name;

    // To test
    // + To get newInstance of class, to use "static" function getIdField
    public Author() {}
    public Author(Long uid, Long id, String username){
        this.uuid = uid;
        this.suid = id;
        this.name = username;
    }

    public Long getUuid() {
        return uuid;
    }

    public Long getSuid() {
        return this.suid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uuid", uuid)
                .append("name", name)
                .toString();
    }

    @Override
    public String getIdField() {
        return FIELD_ID;
    }
}
