package com.app.amber.chat.DATABASE_OPERATIONS.schema;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "user")

public class user {




    @PrimaryKey
    @NonNull

    @ColumnInfo(name="treadId")
    public String threadId;



    @ColumnInfo(name = "user_name")
    public String user_name;


    @ColumnInfo(name = "password")
    public String password;

    public user(String user_name,String password,String threadId) {
        this.user_name = user_name;
        this.threadId=threadId;
        this.password=password;
    }

}
