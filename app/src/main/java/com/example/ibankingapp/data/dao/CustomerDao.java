package com.example.ibankingapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.ibankingapp.entity.CustomerEntity;

import java.util.List;

@Dao
public interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCustomer(CustomerEntity customer);

    @Update
    void updateCustomer(CustomerEntity customer);

    @Delete
    void deleteCustomer(CustomerEntity customer);

    @Query("SELECT * FROM customers_db")
    List<CustomerEntity> getAllCustomers();

    @Query("SELECT * FROM customers_db WHERE accountNumber = :accNumber LIMIT 1")
    CustomerEntity getCustomerByAccountNumber(String accNumber);

    @Query("DELETE FROM customers_db")
    void clearAll();


}
