package com.example.ibankingapp.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ibankingapp.data.dao.CustomerDao;
import com.example.ibankingapp.entity.CustomerEntity;

@Database(entities = {CustomerEntity.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract CustomerDao customerDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "customer_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}