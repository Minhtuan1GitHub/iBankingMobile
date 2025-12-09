package com.example.ibankingapp.ui.transfer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ibankingapp.databinding.ActivityDepositWithdrawBinding;

public class WithdrawFragment extends Fragment {

    private ActivityDepositWithdrawBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ActivityDepositWithdrawBinding.inflate(inflater, container, false);

        binding.tvTitle.setText("RÚT TIỀN");
        binding.btnAction.setText("RÚT TIỀN");

        return binding.getRoot();
    }
}

