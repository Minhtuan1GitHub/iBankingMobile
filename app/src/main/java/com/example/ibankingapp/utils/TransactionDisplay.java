package com.example.ibankingapp.utils;

import com.example.ibankingapp.entity.TransactionEntity;

import java.io.Serializable;

public class TransactionDisplay implements Serializable
    {
    private final TransactionEntity transaction;
    private final String recipientName;

    public TransactionDisplay(TransactionEntity transaction, String recipientName) {
        this.transaction = transaction;
        this.recipientName = recipientName;
    }

    public TransactionEntity getTransaction() {
        return transaction;
    }

    public String getRecipientName() {
        return recipientName;
    }


}
