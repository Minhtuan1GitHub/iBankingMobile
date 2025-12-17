package com.example.ibankingapp.ui.transfer.transaction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ibankingapp.ui.transfer.DepositFragment;
import com.example.ibankingapp.ui.transfer.DepositWithdrawActivity;
import com.example.ibankingapp.ui.transfer.WithdrawFragment;

public class DepositWithdrawAdapter extends FragmentStateAdapter {

    public DepositWithdrawAdapter(@NonNull DepositWithdrawActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new DepositFragment();
        return new WithdrawFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

