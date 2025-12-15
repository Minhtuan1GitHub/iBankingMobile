package com.example.ibankingapp.ui.transfer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivitySuccessfullTransferBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class SuccessfullTransferActivity extends AppCompatActivity {
    private ActivitySuccessfullTransferBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessfullTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String tile = getIntent().getStringExtra("title");
        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        long time = getIntent().getLongExtra("time", 0);
        String name = getIntent().getStringExtra("name");


        //format tien
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(amount);

        //format thoi gian
        String formattedTime = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm:ss", time).toString();

        binding.tvSuccessTitle.setText(tile);
        binding.tvAmountValue.setText(formattedAmount);
        binding.tvRecipientNameValue.setText(name);
        binding.tvRecipientAccountValue.setText(to);
        binding.tvTimeValue.setText(formattedTime);

        binding.btnBackToHome.setOnClickListener(v -> finish());


    }
}
