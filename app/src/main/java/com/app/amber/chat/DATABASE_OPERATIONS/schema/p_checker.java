package com.app.amber.chat.DATABASE_OPERATIONS.schema;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "p_checker")

public class p_checker {




    @PrimaryKey
    @NonNull

    @ColumnInfo(name="treadId")
    public String id;


    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    @ColumnInfo(name = "sms")
    public String sms;


    public p_checker(String id, String sms) {
        this.id = id;
        this.sms =sms;
    }

}
