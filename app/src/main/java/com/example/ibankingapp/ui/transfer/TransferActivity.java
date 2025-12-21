package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.databinding.ActivityTransferBinding;
import com.example.ibankingapp.entity.BankEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.example.ibankingapp.viewModel.customer.InterbankTransferViewModel;
import com.example.ibankingapp.viewModel.customer.InterbankTransferViewModelFactory;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
public class TransferActivity extends AppCompatActivity {

    private ActivityTransferBinding binding;
    private CustomerViewModel viewModel;
    private InterbankTransferViewModel interbankVM;
    private Customer currentCustomer;
    private List<BankEntity> bankList = new ArrayList<>();
    private boolean isRecipientValid = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        interbankVM = new ViewModelProvider(this, new InterbankTransferViewModelFactory(this)).get(InterbankTransferViewModel.class);
        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        loadCurrentCustomer();
        setupReceiverLookup();
        setupBankSpinner();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnTransfer.setOnClickListener(v -> onContinueClicked());
        binding.tabTransferType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    binding.tilBankProvider.setVisibility(View.VISIBLE);
                } else { // Tab Nội bộ
                    binding.tilBankProvider.setVisibility(View.GONE);
                }
                clearFields();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void clearFields() {
        binding.edtRecipientAccount.setText("");
        binding.edtAmount.setText("");
        binding.edtDescription.setText("");
        binding.spinnerBank.setText("");
        binding.tvRecipientName.setVisibility(View.GONE);
        binding.tilRecipientAccount.setError(null);
        isRecipientValid = false;
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
            @Override public void afterTextChanged(Editable s) { }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isRecipientValid = false;
                binding.tvRecipientName.setVisibility(View.GONE);
                binding.tvRecipientName.setText("");
                binding.tilRecipientAccount.setError(null);
                String accountNumber = s.toString().trim();
                if (accountNumber.length() < 2) return;

                boolean isInterbank = binding.tabTransferType.getSelectedTabPosition() == 1;
                if (isInterbank) {
                    lookupInterbankRecipient(accountNumber);
                } else {
                    lookupInternalRecipient(accountNumber);
                }
            }
        });
    }

    private void lookupInternalRecipient(String accountNumber) {
        viewModel.getCustomerByAccountNumber(accountNumber).observe(this, recipient -> {
            String currentInput = binding.edtRecipientAccount.getText().toString().trim();

            if (recipient != null && recipient.getFullName() != null && recipient.getAccountNumber().equals(currentInput)) {
                binding.tvRecipientName.setText(recipient.getFullName().toUpperCase());
                binding.tvRecipientName.setVisibility(View.VISIBLE);
                isRecipientValid = true;
            } else {
                binding.tvRecipientName.setVisibility(View.GONE);
                isRecipientValid = false;
            }
        });
    }

    private void lookupInterbankRecipient(String bankNumber) {
        String bankCode = (String) binding.spinnerBank.getTag();
        if (bankCode == null) return;

        interbankVM.getInterbankAccount(bankCode, bankNumber).observe(this, account -> {
            String currentInput = binding.edtRecipientAccount.getText().toString().trim();

            if (account != null && account.getFullName() != null && account.getAccountNumber().equals(currentInput)) {
                binding.tvRecipientName.setText(account.getFullName().toUpperCase());
                binding.tvRecipientName.setVisibility(View.VISIBLE);
                isRecipientValid = true;
            } else {
                binding.tvRecipientName.setVisibility(View.GONE);
                isRecipientValid = false;
            }
        });
    }

    private void setupBankSpinner() {
        interbankVM.getBanks().observe(this, banks -> {
            if (banks == null) return;
            bankList = banks;
            List<String> bankNames = new ArrayList<>();
            for (BankEntity bank : banks) {
                bankNames.add(bank.getBankName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bankNames);
            binding.spinnerBank.setAdapter(adapter);
        });

        binding.spinnerBank.setOnItemClickListener((parent, view, position, id) -> {
            BankEntity selectedBank = bankList.get(position);
            binding.spinnerBank.setTag(selectedBank.getBankCode()); // Lưu mã bank vào Tag

            // Chọn xong ngân hàng thì thử tra cứu lại ngay nếu đã có số TK
            String currentAcc = binding.edtRecipientAccount.getText().toString().trim();
            if (!currentAcc.isEmpty()) {
                lookupInterbankRecipient(currentAcc);
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

        // Nếu là liên ngân hàng, bắt buộc phải chọn ngân hàng
        if (binding.tabTransferType.getSelectedTabPosition() == 1 && binding.spinnerBank.getText().toString().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngân hàng thụ hưởng", Toast.LENGTH_SHORT).show();
            return;
        }

        // QUAN TRỌNG: Kiểm tra cờ isRecipientValid
        if (!isRecipientValid) {
            binding.tilRecipientAccount.setError("Tài khoản người nhận không hợp lệ hoặc chưa kiểm tra");
            return;
        }

        // Kiểm tra thêm visibility cho chắc chắn
        if (binding.tvRecipientName.getVisibility() != View.VISIBLE) {
            binding.tilRecipientAccount.setError("Tài khoản không tồn tại");
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
            if (binding.tabTransferType.getSelectedTabPosition() == 1) {
                String selectedBank = binding.spinnerBank.getText().toString();
                intent.putExtra("bankName", selectedBank);
            }

            startActivity(intent);

        } catch (NumberFormatException e) {
            binding.tilAmount.setError("Số tiền không hợp lệ");
        }
    }
}