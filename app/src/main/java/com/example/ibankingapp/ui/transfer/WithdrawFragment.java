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

import com.example.ibankingapp.R;
import com.example.ibankingapp.databinding.FragmentWithdrawBinding;

public class WithdrawFragment extends Fragment {

    private FragmentWithdrawBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWithdrawBinding.inflate(inflater, container, false);

        // 1. Setup Chip Group
        setupChipGroup();

        // 2. Button Action
        binding.btnAction.setOnClickListener(v -> handleWithdraw());

        return binding.getRoot();
    }

    private void setupChipGroup() {
        binding.chipGroupAmount.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip50k) {
                binding.edtMoney.setText("50000");
            } else if (checkedId == R.id.chip100k) {
                binding.edtMoney.setText("100000");
            } else if (checkedId == R.id.chip500k) {
                binding.edtMoney.setText("500000");
            }

            if (binding.edtMoney.getText() != null) {
                binding.edtMoney.setSelection(binding.edtMoney.getText().length());
            }
        });
    }

    private void handleWithdraw() {
        String amountStr = binding.edtMoney.getText().toString().trim();

        if (amountStr.isEmpty()) {
            binding.tilMoney.setError("Vui lòng nhập số tiền");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 10000) {
                binding.tilMoney.setError("Số tiền tối thiểu là 10,000 VND");
                return;
            }
            binding.tilMoney.setError(null);

            Intent intent = new Intent(getActivity(), TransactionIntentActivity.class);
            intent.putExtra("amount", String.valueOf((int)amount));
            intent.putExtra("transactionType", "WITHDRAW");
            intent.putExtra("recipientName", "Rút tiền về ngân hàng");
            intent.putExtra("recipientAccount", "Ngân hàng liên kết");
            startActivity(intent);

        } catch (NumberFormatException e) {
            binding.tilMoney.setError("Định dạng số tiền không hợp lệ");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}