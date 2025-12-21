package com.example.ibankingapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ibankingapp.entity.MortageEntity;
import com.example.ibankingapp.entity.MortagePaymentEntity;

@Dao
public interface MortagePaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MortagePaymentEntity payment);

    @Query("SELECT * FROM mortgage_payments WHERE mortgageId = :mortgageId ORDER BY dueDate DESC LIMIT 1")
    LiveData<MortagePaymentEntity> getCurrentPayment(String mortgageId);


    @Query("SELECT * FROM mortgage_payments WHERE mortgageId = :mortgageId AND period = :period LIMIT 1")
    MortagePaymentEntity getPaymentByPeriod(String mortgageId, String period);
}
