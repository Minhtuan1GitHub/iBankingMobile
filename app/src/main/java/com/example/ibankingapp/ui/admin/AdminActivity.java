package com.example.ibankingapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityAdminBinding;
import com.example.ibankingapp.databinding.ActivityHomeBinding;
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

        adminBinding.fabCreateCustomer.setOnClickListener(v->{
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        adminBinding.fabCustomerList.setOnClickListener(v->{
            startActivity(new Intent(this, CustomerListActivity.class));
        });

        adminBinding.fabLogout.setOnClickListener(v->{
            startActivity(new Intent(this, SettingActivity.class));
        });
    }

}

