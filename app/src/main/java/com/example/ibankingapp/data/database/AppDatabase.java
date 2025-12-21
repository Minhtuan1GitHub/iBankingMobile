package com.example.ibankingapp.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ibankingapp.data.dao.CustomerDao;
import com.example.ibankingapp.data.dao.MortageDao;
import com.example.ibankingapp.data.dao.MortagePaymentDao;
import com.example.ibankingapp.data.dao.NotificationDao;
import com.example.ibankingapp.data.dao.SavingAccountDao;
import com.example.ibankingapp.data.dao.TransactionDao;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.entity.MortageEntity;
import com.example.ibankingapp.entity.MortagePaymentEntity;
import com.example.ibankingapp.entity.NotificationEntity;
import com.example.ibankingapp.entity.SavingAccountEntity;
import com.example.ibankingapp.entity.TransactionEntity;

@Database(entities = {CustomerEntity.class,
        TransactionEntity.class,
        SavingAccountEntity.class,
        MortageEntity.class,
        MortagePaymentEntity.class,
        NotificationEntity.class},
        version =21)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract CustomerDao customerDao();
    public abstract TransactionDao transactionDao();
    public abstract SavingAccountDao savingAccountDao();
    public abstract NotificationDao notificationDao();
    public abstract MortageDao mortageDao();
    public abstract MortagePaymentDao mortagePaymentDao();





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