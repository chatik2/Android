package com.shifu.user.truechat.realm;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.concurrent.atomic.AtomicLong;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Users extends RealmObject {

    private static AtomicLong INTEGER_COUNTER = new AtomicLong(0);
    public static final String FIELD_ID = "uid";

    @PrimaryKey
    private Long uid;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUid() {
        return uid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uid", uid)
                .append("name", name)
                .toString();
    }

    public static long increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }

}
