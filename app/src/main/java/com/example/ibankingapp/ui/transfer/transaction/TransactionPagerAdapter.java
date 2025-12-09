package com.example.ibankingapp.ui.transfer.transaction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.ibankingapp.ui.transfer.DepositFragment;
import com.example.ibankingapp.ui.transfer.WithdrawFragment;

public class TransactionPagerAdapter extends FragmentStateAdapter {

    public TransactionPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new DepositFragment();
        else return new WithdrawFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

