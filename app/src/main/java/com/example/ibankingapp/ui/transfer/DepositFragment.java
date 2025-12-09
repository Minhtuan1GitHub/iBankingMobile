package com.example.ibankingapp.ui.transfer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ibankingapp.databinding.ActivityDepositWithdrawBinding;

public class DepositFragment extends Fragment {

    private ActivityDepositWithdrawBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ActivityDepositWithdrawBinding.inflate(inflater, container, false);

        binding.tvTitle.setText("NẠP TIỀN");
        binding.btnAction.setText("NẠP TIỀN");

        return binding.getRoot();
    }
}
