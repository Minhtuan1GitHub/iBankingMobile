package com.example.ibankingapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityRegisterBinding;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.ui.admin.AdminActivity;
import com.example.ibankingapp.viewModel.login.FirebaseAuthManager;
import com.example.ibankingapp.viewModel.login.FirestoreManager;


public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding registerBinding;

    private FirebaseAuthManager authManager = new FirebaseAuthManager();
    private FirestoreManager storeManager = new FirestoreManager();
    private CustomerRepository repository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(registerBinding.getRoot());
        repository = new CustomerRepository(this);


        registerBinding.btnCreateCustomer.setOnClickListener(v -> attemptCustomerRegistration());
    }


    private void attemptCustomerRegistration() {
        // Retrieve and trim all values using the binding object directly
        String emailValue = registerBinding.edtEmail.getText().toString().trim();
        String passwordValue = registerBinding.edtPassword.getText().toString().trim();
        String fullnameValue = registerBinding.edtFullName.getText().toString().trim();
        String accountnumberValue = registerBinding.edtAccountNumber.getText().toString().trim();
        String accountTypeValue = registerBinding.edtAccountType.getText().toString().trim();
        String balanceText = registerBinding.edtBalance.getText().toString().trim();
        String phoneValue = registerBinding.edtPhone.getText().toString().trim();
        String otpValue = registerBinding.edtOtp.getText().toString().trim();


        // 1. Validation

        if (emailValue.isEmpty() || passwordValue.isEmpty() || fullnameValue.isEmpty() ||
                accountnumberValue.isEmpty() || accountTypeValue.isEmpty() || balanceText.isEmpty()) {

            Toast.makeText(this, "Vui lòng điền đầy đủ tất cả thông tin khách hàng.", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Balance Parsing and Validation
        double balanceValue;
        try {
            balanceValue = Double.parseDouble(balanceText);
            if (balanceValue < 0) {
                Toast.makeText(this, "Số dư không được âm. Vui lòng nhập số hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số dư ban đầu phải là một số hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        authManager.register(emailValue, passwordValue, task -> {
            if (task.isSuccessful()) {
                String uid = task.getResult().getUser().getUid();

                saveCustomerDetails(uid, fullnameValue, accountnumberValue, accountTypeValue, balanceValue, phoneValue, otpValue);
            } else {
                Toast.makeText(this, "Lỗi xác thực Firebase: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void saveCustomerDetails(String uid, String fullname, String accountNum, String accountType, double balance, String phone, String otp) {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(uid);
        customer.setFullName(fullname);
        customer.setAccountNumber(accountNum);
        customer.setAccountType(accountType);
        customer.setBalance(balance);
        customer.setPhone(phone);
        customer.setRole("customer");
        customer.setOtp(otp);

        repository.insert(customer);
        Toast.makeText(this, "Tạo tài khoản thành công!", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, AdminActivity.class));
        finish();



    }
}