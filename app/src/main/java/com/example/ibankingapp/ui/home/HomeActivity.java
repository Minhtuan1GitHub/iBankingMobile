package com.example.ibankingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityHomeBinding;
import com.example.ibankingapp.ui.login.RegisterActivity;


public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding homeBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());


    }
}
