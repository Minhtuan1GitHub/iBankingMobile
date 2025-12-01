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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        customerRepository = new CustomerRepository(this);
        customerRepository.listenFirestoreChanges();

        // Tạo adapter rỗng trước
        TransactionAdapter adapter = new TransactionAdapter();
        binding.rvTransactionHistory.setAdapter(adapter);
        binding.rvTransactionHistory.setLayoutManager(new LinearLayoutManager(this));

       // String currentAccount = getIntent().getStringExtra("accountNumber");
        //String currentAccount = "113";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String curentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("customers")
                        .document(curentUserId)
                                .get()
                                        .addOnSuccessListener(doc -> {
                                            if (doc.exists()) {
                                                String currentAccount = doc.getString("accountNumber");
                                                if (currentAccount != null) {
                                                    viewModel.loadTransactions(currentAccount);
                                                }
                                            }
                                        })
                                                .addOnFailureListener(e -> {
                                                    // Xử lý lỗi
                                                });



       // viewModel.loadTransactions(currentAccount);

        viewModel.getTransactions().observe(this, transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                binding.tvNoData.setVisibility(View.GONE);
                adapter.setTransactions(transactions);
//                for (TransactionDisplay t : transactions) {
//                    Log.d("HistoryTransaction", "Recipient: " + t.getRecipientName() + ", STK: " + t.getTransaction().getToAcountNumber());
//                }
            } else {
                binding.tvNoData.setVisibility(View.VISIBLE);
                adapter.setTransactions(Collections.emptyList());
            }
        });

        binding.fabHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
    }



}
