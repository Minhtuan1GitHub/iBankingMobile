package com.example.ibankingapp.ui.transfer.transaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityTransactionDetailBinding;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.entity.TransactionEntity;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.ui.home.HomeActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionDetailActivity extends AppCompatActivity {
    private ActivityTransactionDetailBinding binding;
    private CustomerRepository customerRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        customerRepository = new CustomerRepository(this);

        TransactionEntity transaction = (TransactionEntity) getIntent().getSerializableExtra("transaction");

        if (transaction != null) {
            loadTransactionData(transaction);
        } else {
            finish();
        }

        setupButtons();
    }

    @SuppressLint("SetTextI18n")
    private void loadTransactionData(TransactionEntity transaction) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        binding.textTimestamp.setText(dateFormat.format(new Date(transaction.getTimestamp())));
        binding.textId.setText(transaction.getId());

        String note = transaction.getNote();
        binding.textStatus.setText(note != null && !note.isEmpty() ? note : "Giao dịch thành công");


        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            String fromAcc = transaction.getFromAccountNumber();
            String toAcc = transaction.getToAccountNumber();

            String senderName = fromAcc;
            String receiverName = toAcc;

            // Tìm tên người gửi/nhận từ
            if (fromAcc != null) {
                CustomerEntity sender = customerRepository.getCustomerByAccount(fromAcc);
                if (sender != null && sender.getFullName() != null) senderName = sender.getFullName();
            }

            if (toAcc != null) {
                CustomerEntity receiver = customerRepository.getCustomerByAccount(toAcc);
                if (receiver != null && receiver.getFullName() != null) receiverName = receiver.getFullName();
            }


            String finalSenderName = senderName;
            String finalReceiverName = receiverName;


            boolean isNegative = "WITHDRAW".equals(transaction.getType());
            if (!isNegative && !"DEPOSIT".equals(transaction.getType())) {

                isNegative = true;
            }
            if ("DEPOSIT".equals(transaction.getType())) isNegative = false;

            boolean finalIsNegative = isNegative;

            runOnUiThread(() -> {
                binding.textFromAccount.setText(finalSenderName + "\n" + fromAcc);
                binding.textToAccount.setText(finalReceiverName + "\n" + toAcc);

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                String amountStr = currencyFormat.format(transaction.getAmount());

                if (finalIsNegative) {
                    binding.textAmount.setText("-" + amountStr);
                    binding.textAmount.setTextColor(Color.parseColor("#D32F2F"));
                } else {
                    binding.textAmount.setText("+" + amountStr);
                    binding.textAmount.setTextColor(Color.parseColor("#4CAF50"));
                }
            });
        });
    }

    private void setupButtons() {
        binding.fabHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        binding.btnBack.setOnClickListener(v -> finish());
    }
}