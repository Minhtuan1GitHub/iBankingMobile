package com.example.ibankingapp.ui.transfer.transaction;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityTransactionDetailBinding;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.ui.home.HomeActivity;
import com.example.ibankingapp.utils.TransactionDisplay;

public class TransactionDetailActivity extends AppCompatActivity {
    private ActivityTransactionDetailBinding binding;
    private CustomerRepository customerRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        customerRepository = new CustomerRepository(this);



        TransactionDisplay data = (TransactionDisplay) getIntent().getSerializableExtra("transaction");

        java.util.concurrent.Executor executor = java.util.concurrent.Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            // Lấy dữ liệu trong background thread
            CustomerEntity sender = customerRepository.getCustomerByAccount(data.getTransaction().getFromAcountNumber());
            CustomerEntity receiver = customerRepository.getCustomerByAccount(data.getTransaction().getToAcountNumber());

            String senderName = (sender != null) ? sender.getFullName() : data.getTransaction().getFromAcountNumber();
            String receiverName = (receiver != null) ? receiver.getFullName() : data.getTransaction().getToAcountNumber();

            // Cập nhật UI trên main thread
            runOnUiThread(() -> {
                binding.textFromAccount.setText(data.getTransaction().getFromAcountNumber() + " - " + senderName);
                binding.textToAccount.setText(data.getTransaction().getToAcountNumber() + " - " + receiverName);
                binding.textId.setText(data.getTransaction().getId());
                binding.textAmount.setText(String.valueOf(data.getTransaction().getAmount()));
                binding.textStatus.setText(data.getTransaction().getStatus());
                binding.textTimestamp.setText(String.valueOf(data.getTransaction().getTimestamp()));
            });
        });









        binding.fabHome.setOnClickListener(v->{
            startActivity(new Intent(this, HomeActivity.class));
        });
    }

}
