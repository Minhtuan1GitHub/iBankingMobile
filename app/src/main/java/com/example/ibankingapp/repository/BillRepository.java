package com.example.ibankingapp.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.ibankingapp.entity.BillEntity;
import com.google.firebase.firestore.FirebaseFirestore;

public class BillRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public MutableLiveData<BillEntity> getBillByCode(String code) {
        MutableLiveData<BillEntity> result = new MutableLiveData<>();

        firestore.collection("bills")
                .whereEqualTo("customerCode", code)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        result.postValue(null);
                        return;
                    }

                    BillEntity bill = querySnapshot
                            .getDocuments()
                            .get(0)
                            .toObject(BillEntity.class);

                    result.postValue(bill);
                })
                .addOnFailureListener(e -> result.postValue(null));

        return result;
    }

    public void payBill(
            BillEntity bill,
            String fromAccount,
            double balance,
            BillPaymentCallback callback
    ) {
        if (bill == null || bill.getAmount() == null) {
            callback.onFail("Hóa đơn không hợp lệ");
            return;
        }

        if (balance < bill.getAmount()) {
            callback.onFail("Không đủ số dư");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("bills") // ✅ đúng tên collection
                .whereEqualTo("customerCode", bill.getCustomerCode())
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        callback.onFail("Không tìm thấy hóa đơn");
                        return;
                    }

                    snapshot.getDocuments()
                            .get(0)
                            .getReference()
                            .update("status", "Đã thanh toán")
                            .addOnSuccessListener(a ->
                                    callback.onResult(true, null))
                            .addOnFailureListener(e ->
                                    callback.onResult(false, e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onResult(false, e.getMessage()));
    }

    public interface BillPaymentCallback {
        void onResult(boolean success, String message);
        void onFail(String message);
    }





}
