package com.example.ibankingapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TopupPackageSeeder {

    private static final String SYSTEM_COLLECTION = "system";
    private static final String SEED_DOC = "seed_status";

    public static void seedPackagesOnce() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(SYSTEM_COLLECTION)
                .document(SEED_DOC)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists() && Boolean.TRUE.equals(doc.getBoolean("topup_packages_seeded"))) {
                        // ✅ ĐÃ SEED → KHÔNG LÀM GÌ
                        return;
                    }

                    // ❌ CHƯA SEED → TIẾN HÀNH SEED
                    seed(db, "Viettel", 10000);
                    seed(db, "Viettel", 20000);
                    seed(db, "Viettel", 50000);
                    seed(db, "Viettel", 100000);

                    seed(db, "Mobifone", 10000);
                    seed(db, "Mobifone", 50000);

                    seed(db, "Vinaphone", 20000);
                    seed(db, "Vinaphone", 100000);

                    // ✅ ĐÁNH DẤU ĐÃ SEED
                    Map<String, Object> flag = new HashMap<>();
                    flag.put("topup_packages_seeded", true);

                    db.collection(SYSTEM_COLLECTION)
                            .document(SEED_DOC)
                            .set(flag);
                });
    }

    private static void seed(FirebaseFirestore db, String provider, int amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("provider", provider);
        data.put("amount", amount);
        data.put("price", amount);
        data.put("discount", 0);

        db.collection("topup_packages").add(data);
    }
}
