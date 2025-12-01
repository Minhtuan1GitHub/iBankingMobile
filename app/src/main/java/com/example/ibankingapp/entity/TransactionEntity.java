package com.example.ibankingapp.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions_db")
public class TransactionEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String fromAcountNumber;
    private String toAcountNumber;
    private double amount;
    private long timestamp;
    private String status;

    public TransactionEntity() {}

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getFromAcountNumber() {
        return fromAcountNumber;
    }

    public void setFromAcountNumber(String fromAcountNumber) {
        this.fromAcountNumber = fromAcountNumber;
    }

    public String getToAcountNumber() {
        return toAcountNumber;
    }

    public void setToAcountNumber(String toAcountNumber) {
        this.toAcountNumber = toAcountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
