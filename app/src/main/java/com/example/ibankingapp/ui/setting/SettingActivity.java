package com.example.ibankingapp.ui.setting;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivitySettingBinding;
import com.example.ibankingapp.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executors;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding settingBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingBinding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(settingBinding.getRoot());

        settingBinding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            AppDatabase db = AppDatabase.getInstance(this);
            Executors.newSingleThreadExecutor().execute(() -> {
                db.customerDao().clearAll();
            });

            startActivity(new Intent(this, LoginActivity.class));
            finish();

        });
    }

}
