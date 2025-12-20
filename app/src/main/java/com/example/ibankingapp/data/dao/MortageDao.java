package com.example.ibankingapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.ibankingapp.entity.MortageEntity;

@Dao
public interface MortageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MortageEntity mortage);

    @Query("SELECT * FROM mortgages WHERE customerId = :customerId LIMIT 1")
    LiveData<MortageEntity> getMortgageByCustomerId(String customerId);

    @Query("UPDATE mortgages SET remainingBalance = :remainingBalance, interestRate = :interestRate , termMonths = :termMonths WHERE firebaseId = :firebaseId")
    void updatePayment(String firebaseId, double remainingBalance, double interestRate, int termMonths);



}
