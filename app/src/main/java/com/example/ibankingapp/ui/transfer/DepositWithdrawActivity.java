package com.example.ibankingapp.ui.transfer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityDepositWithdrawBinding;
import com.example.ibankingapp.ui.transfer.transaction.DepositWithdrawAdapter;
import com.google.android.material.tabs.TabLayoutMediator;

public class DepositWithdrawActivity extends AppCompatActivity {

    private ActivityDepositWithdrawBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDepositWithdrawBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Xử lý Toolbar (Nút Back)
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // 2. Setup Adapter cho ViewPager
        DepositWithdrawAdapter adapter = new DepositWithdrawAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // 3. Kết nối TabLayout và ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) tab.setText("NẠP TIỀN");
            else tab.setText("RÚT TIỀN");
        }).attach();

        // 4. Xử lý điều hướng (Mở đúng tab Nạp hoặc Rút)
        handleIntentNavigation();
    }

    private void handleIntentNavigation() {
        // Hỗ trợ cả kiểu int (code cũ) và String (code mới từ SuccessActivity)
        int tabIndex = getIntent().getIntExtra("tab", 0);
        String navigateTo = getIntent().getStringExtra("NAVIGATE_TO");

        if ("WITHDRAW".equals(navigateTo)) {
            tabIndex = 1;
        } else if ("DEPOSIT".equals(navigateTo)) {
            tabIndex = 0;
        }

        binding.viewPager.setCurrentItem(tabIndex, false);
    }
}