package com.example.ibankingapp.ui.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.R;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivitySettingBinding;
import com.example.ibankingapp.ui.account.checking.AccountInfoActivity;
import com.example.ibankingapp.ui.keyc.EkycActivity;
import com.example.ibankingapp.ui.login.LoginActivity;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executors;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding settingBinding;
    private CustomerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingBinding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(settingBinding.getRoot());

        settingBinding.toolbar.setNavigationOnClickListener(v -> finish());


        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUserData(uid);

        setupClickEvents();
    }

    private void loadUserData(String uid) {

        viewModel.getCustomer(uid).observe(this, customer -> {
            if (customer != null) {

                settingBinding.setUserName(customer.getFullName());
                settingBinding.setAccountNumber("STK: " + customer.getAccountNumber());
            }
        });


        viewModel.getImage(uid).observe(this, image -> {
            if (image != null) {
                settingBinding.ivAvatar.setImageURI(Uri.parse(image));
            } else {
                settingBinding.ivAvatar.setImageResource(R.drawable.ic_account_circle_24); // Đổi icon mặc định cho hợp lý
            }
        });
    }

    private void setupClickEvents() {
        // Nút Đăng xuất
        settingBinding.btnLogout.setOnClickListener(v -> {
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

        // Nút Cài đặt eKYC
        settingBinding.btnEkycSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, EkycActivity.class));
        });

        // Nút Tra cứu
        settingBinding.btnLookup.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountInfoActivity.class));
        });
    }
}