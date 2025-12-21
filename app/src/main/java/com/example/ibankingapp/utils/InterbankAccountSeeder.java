package com.example.ibankingapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class InterbankAccountSeeder {

    public static void seedAccounts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        seed(db, "ACB", "123456789", "NGUYEN VAN A");
        seed(db, "VCB", "222333444", "TRAN THI B");
        seed(db, "TCB", "999888777", "LE VAN C");
        seed(db, "BIDV", "111222333", "PHAM THI D");
    }

    private static void seed(FirebaseFirestore db,
                             String bankCode,
                             String accountNumber,
                             String fullName) {

        String docId = bankCode + "_" + accountNumber;

        Map<String, Object> data = new HashMap<>();
        data.put("bankCode", bankCode);
        data.put("accountNumber", accountNumber);
        data.put("fullName", fullName);
        data.put("balance", 0.0);
        data.put("status", "ACTIVE");

        db.collection("interbank_accounts")
                .document(docId)
                .set(data);
    }
}
