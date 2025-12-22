package com.example.ibankingapp.ui.account.checking;

import android.os.Bundle;
import android.text.InputType;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.ibankingapp.databinding.ActivityAccountInfoBinding;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountInfoActivity extends AppCompatActivity {

    private ActivityAccountInfoBinding binding;
    private CustomerViewModel viewModel;
    private Customer currentCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        makeStatusBarTransparent();

        binding = ActivityAccountInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        loadCustomerData();
        setupPhoneUpdate();
    }

    private void loadCustomerData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        viewModel.getCustomer(uid).observe(this, customer -> {
            if (customer != null) {
                this.currentCustomer = customer;
                binding.setCustomer(customer);
            }
        });
    }

    private void setupPhoneUpdate() {
        binding.btnEdit.setOnClickListener(v -> {
            if (currentCustomer != null) {
                showUpdatePhoneDialog();
            } else {
                Toast.makeText(this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showUpdatePhoneDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(currentCustomer.getPhone());
        input.setHint("Nhập số điện thoại mới");

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật số điện thoại")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newPhone = input.getText().toString().trim();
                    if (!newPhone.isEmpty()) {
                        updatePhoneNumber(newPhone);
                    } else {
                        Toast.makeText(this, "Số điện thoại không được để trống", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updatePhoneNumber(String newPhone) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        FirebaseFirestore.getInstance().collection("customers").document(uid)
                .update("phone", newPhone)
                .addOnSuccessListener(aVoid -> {

                    if (currentCustomer instanceof CustomerEntity) {
                        ((CustomerEntity) currentCustomer).setPhone(newPhone);
                        new Thread(() -> {
                            new CustomerRepository(this).updateCustomer(currentCustomer);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                // Refresh lại UI thông qua DataBinding
                                binding.setCustomer(currentCustomer);
                            });
                        }).start();
                    } else {
                        Toast.makeText(this, "Đã cập nhật. Vui lòng tải lại trang.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void makeStatusBarTransparent() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}