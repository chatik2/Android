package com.shifu.user.truechat.model;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmObject;

public class Msg extends RealmObject implements MyRealms {

    private static final String FIELD_ID = "umid";
    private static final String FIELD_MID = "netmid";

    @SerializedName("id")
    @Expose
    private Long netmid;

    private Long umid;

    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("date")
    @Expose
    private Date date;

    @SerializedName("secondUserId")
    @Expose
    private Long suid;


    // Unusable fields from base
    @SerializedName("lastActivityDate")
    @Expose
    private String lastActivityDate;

    @SerializedName("lastRequestDate")
    @Expose
    private String lastRequestDate;

    // To get newInstance of class, to use "static" function getIdField
    public Msg(){}

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("net umid", netmid)
                .append("local umid", umid)
                .append("text", text)
                .append("date", date)
                .append("suid", suid)
                .append("lastActivityDate", lastActivityDate)
                .append("lastRequestDate", lastRequestDate)
                .toString();
    }

    @Override
    public String getIdField() {
        return FIELD_ID;
    }

    public static String getNetIdField() {
        return FIELD_MID;
    }

    public Long getUmid() {
        return umid;
    }

    // In transaction only
    public void setUmid(Long umid) {
        this.umid = umid;
    }

    public String getText() {
        return text;
    }
    public void setText(String data) {
        this.text = data;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public void setSuid(Long suid) {
        this.suid = suid;
    }
    public Long getSuid(){
        return this.suid;
    }


    public Long getNetmid() {
        return netmid;
    }

    public void setNetmid(Long netmid) {
        this.netmid = netmid;
    }
}

