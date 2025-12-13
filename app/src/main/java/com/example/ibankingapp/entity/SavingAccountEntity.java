package com.example.ibankingapp.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "savingAccounts")
public class SavingAccountEntity {

//    @PrimaryKey(autoGenerate = true)
//    private int localId;
    @PrimaryKey
    @NonNull
    private String firebaseId;
    private String customerId;
    private String accountNumber;
    private double balance;
    private double interestRate;
    private Long termMonths;
    private long createdAt;
    private long dueDate;

    // Constructor
    public SavingAccountEntity(String firebaseId, String customerId, String accountNumber,
                               double balance, double interestRate, Long termMonths,
                               long createdAt, long dueDate) {
        this.firebaseId = firebaseId;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
    }

    public SavingAccountEntity(){}

//    public int getLocalId() {
//        return localId;
//    }
//
//    public void setLocalId(int localId) {
//        this.localId = localId;
//    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public Long getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Long termMonths) {
        this.termMonths = termMonths;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }
}
