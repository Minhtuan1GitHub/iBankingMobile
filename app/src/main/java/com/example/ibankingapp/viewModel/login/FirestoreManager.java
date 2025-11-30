package com.example.ibankingapp.viewModel.login;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class FirestoreManager {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void saveCustomerIn4(String uid, String fullName, String accountNumber,
                                String accountType, double balance, String phone, String otp,
                                OnCompleteListener<Void> listener){
        Map<String, Object> data = new HashMap<>();

        data.put("uid", uid);
        data.put("fullName", fullName);
        data.put("accountNumber", accountNumber);
        data.put("accountType", accountType);
        data.put("balance", balance);
        data.put("phone", phone);
        data.put("role", "customer");
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("otp", otp);


        db.collection("customers")
                .document(uid)
                .set(data)
                .addOnCompleteListener(listener);
    }
}