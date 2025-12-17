package com.example.ibankingapp.ui.transfer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityTransactionBinding;
import com.example.ibankingapp.ui.transfer.transaction.TransactionPagerAdapter;
import com.google.android.material.tabs.TabLayoutMediator;

public class TransactionTabActivity extends AppCompatActivity {

    private ActivityTransactionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
    }

    private void setupViewPager() {
        TransactionPagerAdapter adapter = new TransactionPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // Gắn TabLayout với ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0)
                        tab.setText("Nạp tiền");
                    else
                        tab.setText("Rút tiền");
                }
        ).attach();
    }
}

