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

        // Nhận dữ liệu từ Intent
        String title = getIntent().getStringExtra("title");
        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        long time = getIntent().getLongExtra("time", System.currentTimeMillis());
        String description = getIntent().getStringExtra("description");
        String name = getIntent().getStringExtra("name");

        // Format tiền
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(amount);

        // Format thời gian
        String formattedTime = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm:ss", time).toString();

        // Hiển thị thông tin
        if (title != null && !title.isEmpty()) {
            binding.tvSuccessTitle.setText(title);
        } else {
            binding.tvSuccessTitle.setText("GIAO DỊCH THÀNH CÔNG");
        }

        binding.tvAmountValue.setText(formattedAmount);

        if (to != null && !to.isEmpty()) {
            binding.tvRecipientNameValue.setText(to);
        } else {
            binding.tvRecipientNameValue.setText("N/A");
        }

        if (from != null && !from.isEmpty()) {
            binding.tvRecipientAccountValue.setText(from);
        } else {
            binding.tvRecipientAccountValue.setText("N/A");
        }

        binding.tvTimeValue.setText(formattedTime);

        // Hiển thị nội dung giao dịch
        if (description != null && !description.isEmpty()) {
            binding.tvDescValue.setText(description);
        } else {
            binding.tvDescValue.setText("Không có nội dung");
        }

        binding.btnBackToHome.setOnClickListener(v -> finish());

    }
}
