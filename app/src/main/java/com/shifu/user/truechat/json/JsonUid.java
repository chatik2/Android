package com.shifu.user.truechat.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class JsonUid  {

    @SerializedName("uid")
    @Expose
    private Long uid;

    @SerializedName("name")
    @Expose
    private String name;

    public JsonUid(Long uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public Long getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uid", uid)
                .append("name", name)
                .toString();
    }
}
