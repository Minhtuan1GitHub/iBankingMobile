package com.example.ibankingapp.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.entity.TopupEntity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TopupRepository {
    private TransactionRepository transactionRepository;

    public TopupRepository(Context context){
        transactionRepository = new TransactionRepository(AppDatabase.getInstance(context).transactionDao());
    }


    public LiveData<List<TopupEntity>> getAllTopups(String provider) {
        MutableLiveData<List<TopupEntity>> data = new MutableLiveData<>();

        FirebaseFirestore.getInstance()
                .collection("topup_packages")
                .whereEqualTo("provider", provider)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<TopupEntity> list = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        TopupEntity entity = doc.toObject(TopupEntity.class);
                        if (entity != null) {
                            list.add(entity);
                        }
                    }

                    data.setValue(list);
                });

        return data;
    }

    public void topup(String uid, String phone, long price, MutableLiveData<Boolean> result){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference customerRef = db.collection("customers").document(uid);
        DocumentReference phoneRef = db.collection("phones").document(phone);

        db.runTransaction(transaction -> {
                    DocumentSnapshot customerSnap = transaction.get(customerRef);
                    DocumentSnapshot phoneSnap = transaction.get(phoneRef);

                    Long customerBalance = customerSnap.getLong("balance");
                    Long phoneBalance = phoneSnap.getLong("balance");
                    String accountNumber = customerSnap.getString("accountNumber");


                    if (customerBalance == null || phoneBalance == null) {
                        throw new RuntimeException("Balance null");
                    }

                    transaction.update(customerRef, "balance", customerBalance - price);
                    transaction.update(phoneRef, "balance", phoneBalance + price);


                    return accountNumber;
                })
                .addOnSuccessListener(accountNumber -> {

                    // ✅ LƯU LỊCH SỬ GIAO DỊCH
                    transactionRepository.logTransaction(
                            accountNumber, // from
                            phone,            // to
                            price,
                            "SUCCESS",
                            "TOPUP",
                            "Nạp tiền điện thoại"
                    );

                    result.postValue(true);   // ✅ BÁO THÀNH CÔNG
                })
                .addOnFailureListener(e -> {
                    result.postValue(false);
                });
    }

}
