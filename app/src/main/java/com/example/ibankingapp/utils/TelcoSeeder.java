package com.example.ibankingapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TelcoSeeder {
    public static void seedTelcoProvider(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        addProvider(db, "VIETTEL","viettel");
        addProvider(db, "MOBIFONE","mobifone");
        addProvider(db, "VINAPHONE","vinaphone");
    }
    private static void addProvider(FirebaseFirestore db, String code, String name){
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("name", name);
        data.put("active", true);

        db.collection("telco_providers")
                .document(code)
                .set(data);

    }
}
