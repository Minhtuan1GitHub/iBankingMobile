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
    private List<Customer> customers;       // danh sách gốc (dữ liệu nguồn)
    private CustomerAdapter adapter;         // adapter để hiển thị lên RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new CustomerRepository(this);
        customers = new ArrayList<>();

        // Khởi tạo adapter với danh sách rỗng ban đầu
        adapter = new CustomerAdapter(customers, customer -> {
            // Xử lý khi click vào item
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("accountNumber", customer.getAccountNumber());
            startActivity(intent);
        });

        binding.rcvCustomers.setAdapter(adapter);
        binding.rcvCustomers.setLayoutManager(new LinearLayoutManager(this));

        loadCustomers();

        repository.listenFirestoreChanges();

        // Xử lý tìm kiếm
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
     * Load tất cả Customers từ Room và hiển thị mặc định
     */
    private void loadCustomers() {
        new Thread(() -> {
            List<Customer> listFromDb = repository.getAllCustomers();
            customers.clear();
            for (Customer c : listFromDb) {
                // Chỉ lấy user có role là customer
                if ("customer".equals(c.getRole())) {
                    customers.add(c);
                }
            }

            runOnUiThread(() -> {

                if (adapter != null) {
                    adapter.update(customers);
                }
            });
        }).start();
    }

    /**
     * Lọc danh sách theo số tài khoản
     */
    private void filterCustomers(String query) {
        List<Customer> filtered = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(customers);
        } else {

            String lowerQuery = query.toLowerCase();
            for (Customer c : customers) {
                if (c.getAccountNumber() != null &&
                        c.getAccountNumber().toLowerCase().contains(lowerQuery)) {
                    filtered.add(c);
                }
                // (Tùy chọn) Tìm theo tên luôn cho tiện
                else if (c.getFullName() != null &&
                        c.getFullName().toLowerCase().contains(lowerQuery)) {
                    filtered.add(c);
                }
            }
        }

        // Cập nhật adapter với danh sách đã lọc
        adapter.update(filtered);
    }
}