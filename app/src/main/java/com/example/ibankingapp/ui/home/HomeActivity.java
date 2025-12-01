package com.example.ibankingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityHomeBinding;
import com.example.ibankingapp.ui.login.RegisterActivity;
import com.example.ibankingapp.ui.setting.SettingActivity;
import com.example.ibankingapp.ui.transfer.TransferActivity;
import com.example.ibankingapp.ui.transfer.transaction.HistoryTransactionActivity;


public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding homeBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        homeBinding.fabTransfer.setOnClickListener(v->{
            startActivity(new Intent(this, TransferActivity.class));
        });

        homeBinding.fabSetting.setOnClickListener(v->{
            startActivity(new Intent(this, SettingActivity.class));

        });
        homeBinding.fabHistory.setOnClickListener(v->{
            startActivity(new Intent(this, HistoryTransactionActivity.class));
        });
    }
}
