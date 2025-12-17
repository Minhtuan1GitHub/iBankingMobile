package com.example.ibankingapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.HashMap;
import java.util.Map;

public class BillSeeder {
    public static void seedBill(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bills")
                .limit(1)
                .get()
                .addOnSuccessListener(snap->{
                   if (snap.isEmpty()){
                       seedBills(db);
                   }
                });
    }
    private static void seedBills(FirebaseFirestore db){
        Map<String, Object> bill1 = new HashMap<>();
        bill1.put("customerCode", "001");
        bill1.put("customerName", "Nguyen Van A");
        bill1.put("period", "04/2025");
        bill1.put("amount", 4);
        bill1.put("provider", "EVN");
        bill1.put("status", "Chưa thanh toán");

        Map<String, Object> bill2 = new HashMap<>();
        bill2.put("customerCode", "001");
        bill2.put("customerName", "Nguyen Van A");
        bill2.put("period", "03/2025");
        bill2.put("amount", 6);
        bill2.put("provider", "EVN");
        bill2.put("status", "Chưa thanh toán");

        Map<String, Object> bill3 = new HashMap<>();
        bill3.put("customerCode", "002");
        bill3.put("customerName", "Tran Thi B");
        bill3.put("period", "04/2025");
        bill3.put("amount", 5);
        bill3.put("provider", "EVN");
        bill3.put("status", "Chưa thanh toán");


        db.collection("bills").document("EVN_001_042025").set(bill1);
        db.collection("bills").document("EVN_001_032025").set(bill2);
        db.collection("bills").document("EVN_002_042025").set(bill3);
    }
}
