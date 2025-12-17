package com.example.ibankingapp.repository;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;


public class KeycRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface Callback {
        void onSuccess(boolean success);
    }

    public void verifyEkyc(String imageUri,Callback callback){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("status", "verified");
        data.put("method","mock-emulator");
        data.put("verifyAt", System.currentTimeMillis());
        data.put("avatar", imageUri);

        firestore.collection("ekyc")
                .document(uid)
                .set(data)
                .addOnSuccessListener(v->callback.onSuccess(true))
                .addOnFailureListener(e->callback.onSuccess(false));
    }


}
