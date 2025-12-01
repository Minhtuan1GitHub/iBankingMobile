package com.example.ibankingapp.ui.transfer.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ibankingapp.databinding.ActivityHistoryTransactionBinding;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.ui.home.HomeActivity;
import com.example.ibankingapp.utils.TransactionDisplay;
import com.example.ibankingapp.viewModel.transaction.TransactionViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;

public class HistoryTransactionActivity extends AppCompatActivity {

    private ActivityHistoryTransactionBinding binding;
    private TransactionViewModel viewModel;
    private CustomerRepository customerRepository;
    private TransactionAdapter adapter; // <-- khai báo ở đây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        customerRepository = new CustomerRepository(this);
        customerRepository.listenFirestoreChanges();

        // RecyclerView layout
        binding.rvTransactionHistory.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String curentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("customers")
                .document(curentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String currentAccount = doc.getString("accountNumber");
                        if (currentAccount != null) {
                            // Tạo adapter với currentAccount
                            adapter = new TransactionAdapter(currentAccount);
                            binding.rvTransactionHistory.setAdapter(adapter);

                            adapter.setOnTransactionClickListener(transaction -> {
                                Intent intent = new Intent(this, TransactionDetailActivity.class);
                                intent.putExtra("transaction", transaction);
                                startActivity(intent);
                            });

                            viewModel.loadTransactions(currentAccount);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi
                });

        // Observer
        viewModel.getTransactions().observe(this, transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                binding.tvNoData.setVisibility(View.GONE);
                if (adapter != null) {
                    adapter.setTransactions(transactions);
                }
            } else {
                binding.tvNoData.setVisibility(View.VISIBLE);
                if (adapter != null) {
                    adapter.setTransactions(Collections.emptyList());
                }
            }
        });

        binding.fabHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
    }
}

