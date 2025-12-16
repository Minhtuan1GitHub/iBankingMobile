package com.example.ibankingapp.repository;

import android.util.Log;

import com.example.ibankingapp.data.dao.TransactionDao;
import com.example.ibankingapp.entity.TransactionEntity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public TransactionRepository(TransactionDao dao){
        this.transactionDao = dao;
    }

    public void logTransaction(
            String from,
            String to,
            double amount,
            String status,
            String type,
            String note
    ){
        Executors.newSingleThreadExecutor().execute(()->{
            String id = UUID.randomUUID().toString();

            TransactionEntity log = new TransactionEntity();
            log.setId(id);
            log.setFromAcountNumber(from);
            log.setToAcountNumber(to);
            log.setAmount(amount);
            log.setTimestamp(System.currentTimeMillis());
            log.setStatus(status);
            log.setType(type);
            log.setNote(note);


            //room
            transactionDao.insert(log);

            // firestore
            firestore.collection("transactions")
                    .document(id)
                    .set(log)
                    .addOnSuccessListener(a-> Log.d("TransactionRepository", "Success"))
                    .addOnFailureListener(e->Log.d("TransactionRepository", "Fail"));
        });
    }

    public void getAllTransactions(String accountNumber, OnTransactionsLoaded callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<TransactionEntity> transactions = transactionDao.getAllTransactions(accountNumber);
            callback.onLoaded(transactions);
        });
    }

    public interface OnTransactionsLoaded {
        void onLoaded(List<TransactionEntity> transactions);
    }


}
