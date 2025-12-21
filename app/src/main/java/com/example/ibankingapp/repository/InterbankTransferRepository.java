package com.example.ibankingapp.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.entity.BankEntity;
import com.example.ibankingapp.entity.InterbankAccountEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class InterbankTransferRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Context context;

    public InterbankTransferRepository(Context context) {
        this.context = context.getApplicationContext();
    }




    public LiveData<List<BankEntity>> getBanks(){
        MutableLiveData<List<BankEntity>> data = new MutableLiveData<>();

        db.collection("banks")
                .get()
                .addOnSuccessListener(snapshot ->{
                    List<BankEntity> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()){
                        list.add(doc.toObject(BankEntity.class));
                    }
                    data.setValue(list);
                });

        return data;
    }

    public LiveData<InterbankAccountEntity> getInterbankAccount(String bankCode, String bankNumber){
        MutableLiveData<InterbankAccountEntity> data = new MutableLiveData<>();

        String docId = bankCode + "_" + bankNumber;

        db.collection("interbank_accounts")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()){
                        data.setValue(doc.toObject(InterbankAccountEntity.class));
                    }else{
                        data.setValue(null);
                    }
                });

        return data;
    }

    public LiveData<Boolean> transferInterbank(
            String senderAccountNumber,
            String receiverDocId,
            double amount
    ) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        db.collection("customers")
                .whereEqualTo("accountNumber", senderAccountNumber)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        result.setValue(false); // Không tìm thấy tài khoản gửi
                        return;
                    }

                    DocumentReference fromRef = querySnapshot.getDocuments().get(0).getReference();

                    db.runTransaction(transaction -> {
                                // Lấy số dư hiện tại
                                DocumentSnapshot fromSnapshot = transaction.get(fromRef);
                                Double fromBalanceObj = fromSnapshot.getDouble("balance");
                                if (fromBalanceObj == null) {
                                    result.postValue(false);
                                    return null;
                                }
                                double fromBalance = fromBalanceObj;

                                if (fromBalance < amount) {
                                    result.postValue(false); // Không đủ số dư
                                    return null;
                                }

                                // Lấy tài khoản nhận
                                DocumentReference toRef = db.collection("interbank_accounts").document(receiverDocId);
                                DocumentSnapshot toSnapshot = transaction.get(toRef);

                                if (!toSnapshot.exists()) {
                                    result.postValue(false); // TK nhận không tồn tại
                                    return null;
                                }

                                Double toBalanceObj = toSnapshot.getDouble("balance");
                                if (toBalanceObj == null) {
                                    result.postValue(false);
                                    return null;
                                }
                                double toBalance = toBalanceObj;

                                // Update số dư
                                transaction.update(fromRef, "balance", fromBalance - amount);
                                transaction.update(toRef, "balance", toBalance + amount);



                                return null;
                            })
                            .addOnSuccessListener(v -> {
                                result.setValue(true);
                                TransactionRepository repo = new TransactionRepository(AppDatabase.getInstance(context).transactionDao());
                                repo.logTransaction(
                                        senderAccountNumber,
                                        receiverDocId,
                                        amount,
                                        "success",
                                        "interbank",
                                        "Chuyển tiền liên ngân hàng"
                                );


                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                result.setValue(false);
                            });
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    result.setValue(false);
                });

        return result;
    }








}
