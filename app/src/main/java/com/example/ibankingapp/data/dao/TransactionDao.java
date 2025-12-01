package com.example.ibankingapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ibankingapp.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(TransactionEntity transaction);

    // all transaction of current user
    @Query("SELECT * FROM transactions_db WHERE fromAcountNumber = :accountNumber OR toAcountNumber = :accountNumber ORDER BY timestamp DESC")
    List<TransactionEntity> getAllTransactions(String accountNumber);



}
