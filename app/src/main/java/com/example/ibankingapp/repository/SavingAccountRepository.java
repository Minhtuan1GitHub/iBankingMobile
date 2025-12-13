package com.example.ibankingapp.repository;

import androidx.lifecycle.LiveData;

import com.example.ibankingapp.data.dao.SavingAccountDao;
import com.example.ibankingapp.entity.SavingAccountEntity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class SavingAccountRepository {

    private final SavingAccountDao dao;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SavingAccountRepository(SavingAccountDao dao){
        this.dao = dao;
    }

    public void createSavingAccount(SavingAccountEntity account){
        //String docId = db.collection("saving_accounts").document().getId();

        String docId = account.getCustomerId();
        Map<String, Object> data = new HashMap<>();
        data.put("customer_id", account.getCustomerId());
        data.put("accountNumber", account.getAccountNumber());
        data.put("balance", account.getBalance());
        data.put("interestRate", account.getInterestRate());
        data.put("termMonths", account.getTermMonths());
        data.put("createdAt", account.getCreatedAt());
        data.put("dueDate", account.getDueDate());

        db.collection("savingAccounts").document(docId).set(data);

        SavingAccountEntity local = new SavingAccountEntity(
                docId,
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getInterestRate(),
                account.getTermMonths(),
                account.getCreatedAt(),
                account.getDueDate()
                );
        dao.insert(local);


    }

    public LiveData<SavingAccountEntity> getSavingAccounts(String customerId) {
        return dao.getSavingAccountsByCustomerId(customerId);
    }

    public void updateSavingAccount(SavingAccountEntity account) {

        Map<String, Object> data = new HashMap<>();
        data.put("balance", account.getBalance());
        data.put("interestRate", account.getInterestRate());
        data.put("dueDate", account.getDueDate());
        data.put("termMonths", account.getTermMonths());


        db.collection("savingAccounts")
                .document(account.getFirebaseId())
                .update(data)
                .addOnSuccessListener(v ->
                        Executors.newSingleThreadExecutor().execute(() ->
                                dao.update(
                                        account.getFirebaseId(),
                                        account.getBalance(),
                                        account.getInterestRate(),
                                        account.getDueDate(),
                                        account.getTermMonths()
                                )
                        )
                );
    }



    public void syncFromFirestore(String customerId) {
        db.collection("savingAccounts")
                .document(customerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    SavingAccountEntity acc = new SavingAccountEntity();

                    acc.setFirebaseId(doc.getId());
                    acc.setCustomerId(doc.getString("customer_id"));
                    acc.setAccountNumber(doc.getString("accountNumber"));
                    acc.setBalance(doc.getDouble("balance"));
                    acc.setInterestRate(doc.getDouble("interestRate"));
                    acc.setTermMonths(doc.getLong("termMonths"));   // ✔ Firestore Long
                    acc.setCreatedAt(doc.getLong("createdAt"));
                    acc.setDueDate(doc.getLong("dueDate"));

                    Executors.newSingleThreadExecutor().execute(() -> dao.insert(acc));
                });
    }





}
