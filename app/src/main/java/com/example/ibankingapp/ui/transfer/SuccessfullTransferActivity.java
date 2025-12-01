package com.example.ibankingapp.ui.transfer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivitySuccessfullTransferBinding;

public class SuccessfullTransferActivity extends AppCompatActivity {
    private ActivitySuccessfullTransferBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessfullTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
}
