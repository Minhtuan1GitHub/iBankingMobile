package com.example.ibankingapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityAdminBinding;
import com.example.ibankingapp.ui.customerList.CustomerListActivity;
import com.example.ibankingapp.ui.login.RegisterActivity;
import com.example.ibankingapp.ui.setting.SettingActivity;

public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding adminBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adminBinding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(adminBinding.getRoot());

        adminBinding.cardCreateCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        adminBinding.cardCustomerList.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerListActivity.class));
        });

        //Quản lý lãi suất
        adminBinding.cardInterestRate.setOnClickListener(v -> {
            // TODO: Tạo Activity ManageInterestRateActivity sau
            Toast.makeText(this, "Chức năng cập nhật lãi suất đang phát triển", Toast.LENGTH_SHORT).show();
        });

        adminBinding.cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingActivity.class));
        });
    }
}