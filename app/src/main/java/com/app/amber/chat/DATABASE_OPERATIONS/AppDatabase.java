package com.app.amber.chat.DATABASE_OPERATIONS;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.app.amber.chat.DATABASE_OPERATIONS.opearations.AllDatabaseOperations;
import com.app.amber.chat.DATABASE_OPERATIONS.schema.p_checker;
import com.app.amber.chat.DATABASE_OPERATIONS.schema.user;


@Database(entities = {user.class,p_checker.class}, version = 7,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract AllDatabaseOperations applicationDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "video_chat")
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }





}
