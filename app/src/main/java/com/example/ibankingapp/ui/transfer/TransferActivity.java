package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.databinding.ActivityTransferBinding;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TransferActivity extends AppCompatActivity {

    private ActivityTransferBinding binding;
    private CustomerViewModel viewModel;
    private Customer currentCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        loadCurrentCustomer();
        setupReceiverLookup();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnTransfer.setOnClickListener(v -> onContinueClicked());
    }

    @SuppressLint("SetTextI18n")
    private void loadCurrentCustomer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        viewModel.getCustomer(user.getUid()).observe(this, customer -> {
            if (customer != null) {
                currentCustomer = customer;
                String formattedBalance = String.format("%,.0f", customer.getBalance());
                binding.tvSourceBalance.setText(formattedBalance + " VND");
            } else {
                Toast.makeText(this, "Lỗi tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupReceiverLookup() {
        binding.edtRecipientAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String accountNumber = s.toString().trim();
                if (accountNumber.isEmpty()) {
                    binding.tvRecipientName.setVisibility(View.GONE);
                    return;
                }
                if (accountNumber.length() >= 2) {
                    lookupRecipient(accountNumber);
                }
            }
        });
    }

    private void lookupRecipient(String accountNumber) {
        viewModel.getCustomerByAccountNumber(accountNumber).observe(this, recipient -> {
            if (recipient != null && recipient.getFullName() != null) {
                binding.tvRecipientName.setText(recipient.getFullName().toUpperCase());
                binding.tvRecipientName.setVisibility(View.VISIBLE);
                binding.tilRecipientAccount.setError(null);
            } else {
                binding.tvRecipientName.setVisibility(View.GONE);
            }
        });
    }

    private void onContinueClicked() {
        if (currentCustomer == null) {
            Toast.makeText(this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            return;
        }

        String toAccount = binding.edtRecipientAccount.getText().toString().trim();
        String amountStr = binding.edtAmount.getText().toString().trim();
        String content = binding.edtDescription.getText().toString().trim();

        if (toAccount.isEmpty()) {
            binding.tilRecipientAccount.setError("Vui lòng nhập tài khoản nhận");
            return;
        }

        if (binding.tvRecipientName.getVisibility() != View.VISIBLE) {
            binding.tilRecipientAccount.setError("Tài khoản người nhận không hợp lệ");
            return;
        }

        if (amountStr.isEmpty()) {
            binding.tilAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 10000) {
                binding.tilAmount.setError("Số tiền tối thiểu là 10,000 VND");
                return;
            }
            if (amount > currentCustomer.getBalance()) {
                binding.tilAmount.setError("Số dư không đủ");
                return;
            }
            binding.tilAmount.setError(null);


            Intent intent = new Intent(this, TransactionIntentActivity.class);
            intent.putExtra("transactionType", "TRANSFER");
            intent.putExtra("amount", amountStr);
            intent.putExtra("recipientAccount", toAccount);
            intent.putExtra("recipientName", binding.tvRecipientName.getText().toString());
            intent.putExtra("content", content);

            startActivity(intent);

        } catch (NumberFormatException e) {
            binding.tilAmount.setError("Số tiền không hợp lệ");
        }
    }
}