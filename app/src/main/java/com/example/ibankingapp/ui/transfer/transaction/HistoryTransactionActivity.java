package com.example.ibankingapp.ui.transfer.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ibankingapp.databinding.ActivityHistoryTransactionBinding;
import com.example.ibankingapp.ui.home.HomeActivity;
import com.example.ibankingapp.ui.maps.MapsActivity;
import com.example.ibankingapp.ui.setting.SettingActivity;
import com.example.ibankingapp.ui.transfer.TransferActivity;
import com.example.ibankingapp.viewModel.transaction.TransactionViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;

public class HistoryTransactionActivity extends AppCompatActivity {

    private ActivityHistoryTransactionBinding binding;
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        binding.rvTransactionHistory.setLayoutManager(new LinearLayoutManager(this));

        binding.btnBack.setOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        loadUserAndSetupAdapter();
    }

    private void loadUserAndSetupAdapter() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore.getInstance().collection("customers")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String currentAccount = doc.getString("accountNumber");
                        if (currentAccount != null) {
                            setupAdapter(currentAccount);
                        } else {
                            showEmptyState();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void setupAdapter(String currentAccount) {

        adapter = new TransactionAdapter(currentAccount);
        binding.rvTransactionHistory.setAdapter(adapter);

        // click item
        adapter.setOnTransactionClickListener(transactionDisplay -> {
            Intent intent = new Intent(this, TransactionDetailActivity.class);
            intent.putExtra("transaction", transactionDisplay.getTransaction());
            startActivity(intent);
        });


        viewModel.loadTransactions(currentAccount);

        viewModel.getTransactions().observe(this, transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                binding.tvNoData.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.GONE); // Nếu có layoutEmpty
                adapter.setTransactions(transactions);
            } else {
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        binding.tvNoData.setVisibility(View.VISIBLE);
        if (binding.layoutEmpty != null) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        }
        if (adapter != null) {
            adapter.setTransactions(Collections.emptyList());
        }
    }

    private void setupBottomNavigation() {
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        binding.fabTransfers.setOnClickListener(v -> startActivity(new Intent(this, TransferActivity.class)));
        binding.navMap.setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));
        binding.navProfile.setOnClickListener(v -> startActivity(new Intent(this, SettingActivity.class)));
    }
}