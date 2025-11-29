package com.example.ibankingapp.viewModel.login;

import android.media.MediaPlayer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class FirebaseAuthManager {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public void login(String email, String pass, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(listener);
    }

    public void register(String email, String pass, OnCompleteListener<AuthResult> listener){
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(listener);
    }


}
