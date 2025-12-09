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

        // Adapter
        DepositWithdrawAdapter adapter = new DepositWithdrawAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // TabLayout + ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) tab.setText("NẠP TIỀN");
            else tab.setText("RÚT TIỀN");
        }).attach();

        // Lấy tab được truyền từ HomeActivity
        int tabIndex = getIntent().getIntExtra("tab", 0);
        binding.viewPager.setCurrentItem(tabIndex, false);
    }
}

