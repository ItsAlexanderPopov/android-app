package com.example.easysale.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.easysale.model.User;

@Database(entities = {User.class}, version = 3)
public abstract class UserDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    private static volatile UserDatabase INSTANCE;

    static UserDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UserDatabase.class, "user_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
