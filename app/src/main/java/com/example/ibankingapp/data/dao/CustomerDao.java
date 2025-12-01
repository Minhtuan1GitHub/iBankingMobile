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
        CustomerEntity sender = getCustomerByAccountNumber(from);
        CustomerEntity receiver = getCustomerByAccountNumber(to);

        if (sender == null || receiver == null) return false;
        if (sender.getBalance() < amount) return false;

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        updateCustomer(sender);
        updateCustomer(receiver);

//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("customers").document(sender.getId()).set(sender);
//        db.collection("customers").document(receiver.getId()).set(receiver);




        return true;
    }

    @Query("SELECT * FROM customers_db WHERE accountNumber = :accountNumber LIMIT 1")
    CustomerEntity getCustomerByAccount(String accountNumber);


}
