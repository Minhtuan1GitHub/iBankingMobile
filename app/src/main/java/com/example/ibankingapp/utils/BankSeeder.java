package com.example.ibankingapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class BankSeeder {

    public static void seedBanks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        seed(db, "VCB", "Vietcombank");
        seed(db, "ACB", "ACB Bank");
        seed(db, "TCB", "Techcombank");
        seed(db, "BIDV", "BIDV");
        seed(db, "MB", "MB Bank");
    }

    private static void seed(FirebaseFirestore db,
                             String bankCode,
                             String bankName) {

        Map<String, Object> data = new HashMap<>();
        data.put("bankCode", bankCode);
        data.put("bankName", bankName);

        db.collection("banks")
                .document(bankCode)
                .set(data);
    }
}
