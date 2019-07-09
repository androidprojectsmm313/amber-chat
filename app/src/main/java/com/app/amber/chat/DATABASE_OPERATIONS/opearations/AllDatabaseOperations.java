package com.app.amber.chat.DATABASE_OPERATIONS.opearations;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.app.amber.chat.DATABASE_OPERATIONS.schema.p_checker;
import com.app.amber.chat.DATABASE_OPERATIONS.schema.user;

import java.util.List;


@Dao
    public abstract class AllDatabaseOperations{



    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract  public void insertUser(user u);

    @Delete
    abstract  public void deleteAllUsers(List<user> users);

    @Query("SELECT * FROM user")
    abstract public List<user> getAllUsers();



    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract  public void insertPChecker(p_checker p);

    @Delete
    abstract  public void deleteAllPChecker(List<p_checker> users);

    @Query("SELECT * FROM p_checker")
    abstract public List<p_checker> getAllPChecker();

}



