package com.example.ibankingapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityAdminBinding;
import com.example.ibankingapp.ui.customerList.CustomerListActivity;
import com.example.ibankingapp.ui.login.LoginActivity;
import com.example.ibankingapp.ui.login.RegisterActivity;
import com.example.ibankingapp.ui.setting.SettingActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executors;

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
            FirebaseAuth.getInstance().signOut();
            AppDatabase db = AppDatabase.getInstance(this);
            Executors.newSingleThreadExecutor().execute(() -> {
                db.customerDao().clearAll();
            });
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa stack để không back lại được
            startActivity(intent);
            finish();
        });
    }
}