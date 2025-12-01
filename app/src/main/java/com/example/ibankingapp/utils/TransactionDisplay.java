package com.example.ibankingapp.utils;

import com.example.ibankingapp.entity.TransactionEntity;

public class TransactionDisplay {
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
