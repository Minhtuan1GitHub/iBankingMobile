package com.example.ibankingapp.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.ibankingapp.entity.CustomerEntity;
import com.google.firebase.firestore.FirebaseFirestore;

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

    @Transaction
    default boolean transfer(String from, String to, double amount){
        android.util.Log.d("CustomerDao", "transfer() - From: " + from + ", To: " + to + ", Amount: " + amount);

        CustomerEntity sender = getCustomerByAccountNumber(from);
        CustomerEntity receiver = getCustomerByAccountNumber(to);

        android.util.Log.d("CustomerDao", "Sender from DB: " + (sender != null ? sender.getFullName() + " (Balance: " + sender.getBalance() + ")" : "NULL"));
        android.util.Log.d("CustomerDao", "Receiver from DB: " + (receiver != null ? receiver.getFullName() : "NULL"));

        if (sender == null || receiver == null) {
            android.util.Log.e("CustomerDao", "Transfer FAILED - Sender or Receiver is NULL");
            return false;
        }

        if (sender.getBalance() < amount) {
            android.util.Log.e("CustomerDao", "Transfer FAILED - Insufficient balance. Current: " + sender.getBalance() + ", Required: " + amount);
            return false;
        }

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        updateCustomer(sender);
        updateCustomer(receiver);

        android.util.Log.d("CustomerDao", "Transfer SUCCESS - Updated balances in Room DB");

        return true;
    }

    @Query("SELECT * FROM customers_db WHERE accountNumber = :accountNumber LIMIT 1")
    CustomerEntity getCustomerByAccount(String accountNumber);


}
