package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityTransactionIntentBinding;


public class TransactionIntentActivity extends AppCompatActivity {
    private ActivityTransactionIntentBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionIntentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Intent intent = getIntent();

        binding.tvAmountValue.setText(intent.getStringExtra("amount") + " VND");
        binding.tvRecipientNameValue.setText(intent.getStringExtra("recipientName"));
        binding.tvRecipientAccountValue.setText(intent.getStringExtra("recipientAccount"));

    }
}
