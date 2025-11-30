package com.example.ibankingapp.ui.customerList;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.databinding.ActivityCustomerDetailBinding;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;

public class CustomerDetailActivity extends AppCompatActivity {

    private ActivityCustomerDetailBinding customerDetailBinding;
    private CustomerViewModel viewModelCustomerDetail;
    private Customer currentCustomer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customerDetailBinding = ActivityCustomerDetailBinding.inflate(getLayoutInflater());
        setContentView(customerDetailBinding.getRoot());

        viewModelCustomerDetail = new ViewModelProvider(this).get(CustomerViewModel.class);

        String accountNumberCustomer = getIntent().getStringExtra("accountNumber");
        if (accountNumberCustomer == null){
            Toast.makeText(this, "Không tìm thấy khách hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModelCustomerDetail.getCustomerByAccountNumber(accountNumberCustomer).observe(this, customer ->{
            if (customer == null) {
                return;
            }
            currentCustomer = customer;
            customerDetailBinding.edtName.setText(currentCustomer.getFullName());
            customerDetailBinding.edtPhone.setText(currentCustomer.getPhone());
            customerDetailBinding.tbBalance.setText(String.valueOf(currentCustomer.getBalance()));
            customerDetailBinding.tbAccountNumberValue.setText(currentCustomer.getAccountNumber());
            customerDetailBinding.tbAccountType.setText(currentCustomer.getAccountType());
            customerDetailBinding.edtOtp.setText(currentCustomer.getOtp());

        });
        customerDetailBinding.btnSave.setOnClickListener(v->updateCustomer());



    }

    private void updateCustomer() {
        if (currentCustomer == null) {
            Toast.makeText(this, "Chưa load xong dữ liệu khách hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = customerDetailBinding.edtName.getText().toString();
        String phone = customerDetailBinding.edtPhone.getText().toString();
        String otp = customerDetailBinding.edtOtp.getText().toString();

        currentCustomer.setFullName(name);
        currentCustomer.setPhone(phone);
        currentCustomer.setOtp(otp);


        viewModelCustomerDetail.updateCustomer(currentCustomer);

        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }


}
