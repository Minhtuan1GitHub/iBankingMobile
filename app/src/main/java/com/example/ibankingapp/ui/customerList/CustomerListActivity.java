package com.example.ibankingapp.ui.customerList;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ibankingapp.databinding.ActivityCustomerListBinding;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.ui.admin.AdminActivity;

import java.util.ArrayList;
import java.util.List;

public class CustomerListActivity extends AppCompatActivity {

    private ActivityCustomerListBinding binding;
    private CustomerRepository repository;
    private List<Customer> customers;       // danh sách gốc
    private CustomerAdapter adapter;         // adapter để hiển thị lên RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new CustomerRepository(this);
        customers = new ArrayList<>();

        adapter = new CustomerAdapter(customers, customer -> {
            // xử lý khi click vào customer
            // ...
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("accountNumber", customer.getAccountNumber());
            startActivity(intent);
        });

        binding.rcvCustomers.setAdapter(adapter);
        binding.rcvCustomers.setLayoutManager(new LinearLayoutManager(this));

        loadCustomers();              // load từ Room
        repository.listenFirestoreChanges();  // sync với Firestore realtime

        // SEARCH — lọc theo accountNumber
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCustomers(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });



        binding.fabHome.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminActivity.class));
        });
    }

    /**
     * Load tất cả Customers từ Room
     */
    private void loadCustomers() {
        new Thread(() -> {
            customers.clear();
            //customers.addAll(repository.getAllCustomers());
            for (Customer c: repository.getAllCustomers()){
                if ("customer".equals(c.getRole())){
                    customers.add(c);
                }
            }
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }).start();   // 🔥 QUAN TRỌNG: phải start thread
    }

    /**
     * Lọc theo accountNumber
     */
    private void filterCustomers(String query) {
        List<Customer> filtered = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            filtered.addAll(customers);   // nếu search rỗng → hiện toàn bộ
        } else {
            for (Customer c : customers) {
                if (c.getAccountNumber() != null &&
                        c.getAccountNumber().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(c);
                }
            }
        }

        adapter.update(filtered);   // update adapter
    }
}
