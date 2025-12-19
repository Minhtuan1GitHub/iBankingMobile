package com.example.ibankingapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PhoneSeeder {

    public static void seedPhone() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        seedIfNotExists(db, "0382494117", createPhone(
                "0382494117", "Viettel", "Nguyen Van A", 1_000_000
        ));

        seedIfNotExists(db, "0382117494", createPhone(
                "0382117494", "Viettel", "Nguyen Van B", 1_000_000
        ));
    }

    private static void seedIfNotExists(
            FirebaseFirestore db,
            String docId,
            Map<String, Object> data
    ) {
        db.collection("phones")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        db.collection("phones").document(docId).set(data);
                    }
                });
    }

    private static Map<String, Object> createPhone(
            String phone,
            String provider,
            String name,
            long balance
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put("phone", phone);
        map.put("provider", provider);
        map.put("name", name);
        map.put("status", "Đang sử dụng");
        map.put("balance", balance);
        return map;
    }
}
