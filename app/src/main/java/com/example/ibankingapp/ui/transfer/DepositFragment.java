package com.example.ibankingapp.ui.transfer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ibankingapp.databinding.FragmentDepositBinding;

public class DepositFragment extends Fragment {

    private FragmentDepositBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDepositBinding.inflate(inflater, container, false);

        binding.btnAction.setOnClickListener(v -> {
            String amount = binding.edtMoney.getText().toString().trim();

            if (amount.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(getActivity(), TransactionIntentActivity.class);
            intent.putExtra("amount", amount);
            intent.putExtra("transactionType", "DEPOSIT");
            intent.putExtra("recipientName", "Nạp tiền vào tài khoản");
            intent.putExtra("recipientAccount", "Zalo Pay");
            startActivity(intent);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
