package com.example.ibankingapp.ui.account.saving;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityCreateSavingAccountBinding;
import com.example.ibankingapp.entity.SavingAccountEntity;
import com.example.ibankingapp.repository.SavingAccountRepository;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModel;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class CreateSavingAccountActivity extends AppCompatActivity {
    private ActivityCreateSavingAccountBinding binding;
    private SavingAccountViewModel savingAccountViewModel;
    private CustomerViewModel customerViewModel;
    private String customerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateSavingAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Get Current User ID
        customerId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if(customerId == null) {
            Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Init ViewModels
        initViewModels();

        // Setup Spinner Logic (Auto-update Interest Rate)
        setupTermSpinner();
        binding.toolbar.setOnClickListener(v -> finish());

        binding.btnCreateSaving.setOnClickListener(v -> createSavingAccount());
    }

    private void initViewModels() {
        // Saving Account ViewModel
        SavingAccountRepository repo = new SavingAccountRepository(
                AppDatabase.getInstance(this).savingAccountDao());
        SavingAccountViewModelFactory factory = new SavingAccountViewModelFactory(repo);
        savingAccountViewModel = new ViewModelProvider(this, factory).get(SavingAccountViewModel.class);

        // Customer ViewModel (to deduct money from wallet)
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
    }

    private void setupTermSpinner() {
        // Assuming @array/saving_terms is like: ["1 tháng", "3 tháng", "6 tháng", "12 tháng"]
        // We can set a listener to update interest rate automatically
        binding.spnTerm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTerm = parent.getItemAtPosition(position).toString();
                double rate = getInterestRateForTerm(selectedTerm);
                binding.edtInterestRate.setText(String.valueOf(rate));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private double getInterestRateForTerm(String termString) {
        // Extract number from string (e.g., "6 tháng" -> 6)
        int term = 0;
        try {
            term = Integer.parseInt(termString.replaceAll("[^0-9]", ""));
        } catch (Exception e) { return 0.0; }

        // Simple logic for rate calculation
        if (term < 3) return 3.5;
        if (term < 6) return 4.5;
        if (term < 12) return 5.5;
        return 6.8; // >= 12 months
    }

    private void createSavingAccount() {
        String amountStr = binding.edtAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            binding.edtAmount.setError("Vui lòng nhập số tiền");
            return;
        }

        double amount = Double.parseDouble(amountStr);

        // Basic Validation
        if (amount < 100000) {
            binding.edtAmount.setError("Số tiền tối thiểu là 100.000 VND");
            return;
        }

        // Check Wallet Balance
        customerViewModel.getCustomer(customerId).observe(this, customer -> {
            if (customer == null) return;

            if (customer.getBalance() < amount) {
                Toast.makeText(this, "Số dư ví không đủ để mở sổ", Toast.LENGTH_LONG).show();
            } else {
                // Proceed to create account
                processCreation(amount);
            }
            // Remove observer to prevent multiple calls if user clicks again quickly
            customerViewModel.getCustomer(customerId).removeObservers(this);
        });
    }

    private void processCreation(double amount) {
        // 1. Get Term and Interest Rate
        String termString = binding.spnTerm.getSelectedItem().toString();
        long termMonths = 0;
        try {
            termMonths = Long.parseLong(termString.replaceAll("[^0-9]", ""));
        } catch (Exception e) { termMonths = 1; }

        double interestRate = Double.parseDouble(binding.edtInterestRate.getText().toString());

        // 2. Generate Data
        String accountNumber = "STK" + ((int)(Math.random() * 900000) + 100000);
        long createdAt = System.currentTimeMillis();

        // Calculate Due Date
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(createdAt);
        calendar.add(Calendar.MONTH, (int) termMonths);
        long dueDate = calendar.getTimeInMillis();

        SavingAccountEntity account = new SavingAccountEntity(
                "", // Firebase ID will be set by repository or generated
                customerId,
                accountNumber,
                amount,
                interestRate,
                termMonths,
                createdAt,
                dueDate
        );

        // 3. Deduct Money from Wallet
        customerViewModel.walletWithdraw(customerId, amount).observe(this, result -> {
            if (result.isSuccess()) {
                // 4. Create Saving Account in DB
                savingAccountViewModel.createSavingAccount(account);

                Toast.makeText(this, "Mở sổ tiết kiệm thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi trừ tiền: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}