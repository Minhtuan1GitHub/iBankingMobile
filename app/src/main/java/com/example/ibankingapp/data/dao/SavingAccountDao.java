package com.example.ibankingapp.data.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ibankingapp.entity.SavingAccountEntity;



@Dao
public interface SavingAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SavingAccountEntity savingAccount);

    @Query("SELECT * FROM savingAccounts WHERE customerId = :customerId LIMIT 1")
    LiveData<SavingAccountEntity> getSavingAccountsByCustomerId(String customerId);

    @Query("DELETE FROM savingAccounts")
    void deleteAll();

//    @Update
//    void update(SavingAccountEntity savingAccounts);
    @Query("UPDATE savingAccounts SET balance = :balance, interestRate = :interestRate, dueDate = :dueDate , termMonths = :termMonths WHERE firebaseId = :firebaseId")
    void update(String firebaseId, double balance, double interestRate, long dueDate , Long termMonths);

}
