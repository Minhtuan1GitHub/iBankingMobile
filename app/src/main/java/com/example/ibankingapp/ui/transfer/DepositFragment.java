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
import com.example.ibankingapp.databinding.FragmentDepositBinding;
import com.google.android.material.chip.Chip;

public class DepositFragment extends Fragment {

    private FragmentDepositBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDepositBinding.inflate(inflater, container, false);

        // 1. Xử lý sự kiện chọn Chip (50k, 100k...)
        setupChipGroup();

        // 2. Xử lý nút Tiếp tục
        binding.btnAction.setOnClickListener(v -> handleDeposit());

        return binding.getRoot();
    }

    private void setupChipGroup() {
        binding.chipGroupAmount.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip50k) {
                binding.edtMoney.setText("50000");
            } else if (checkedId == R.id.chip100k) {
                binding.edtMoney.setText("100000");
            } else if (checkedId == R.id.chip200k) {
                binding.edtMoney.setText("200000");
            } else if (checkedId == R.id.chip500k) {
                binding.edtMoney.setText("500000");
            } else if (checkedId == R.id.chip1m) {
                binding.edtMoney.setText("1000000");
            }
            // Đặt con trỏ về cuối dòng
            if (binding.edtMoney.getText() != null) {
                binding.edtMoney.setSelection(binding.edtMoney.getText().length());
            }
        });
    }

    private void handleDeposit() {
        String amountStr = binding.edtMoney.getText().toString().trim();

        // Validation
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
            // Xóa lỗi nếu hợp lệ
            binding.tilMoney.setError(null);

            // Chuyển sang màn hình xác nhận
            Intent intent = new Intent(getActivity(), TransactionIntentActivity.class);
            intent.putExtra("amount", String.valueOf((int)amount));
            intent.putExtra("transactionType", "DEPOSIT");
            intent.putExtra("recipientName", "Nạp tiền vào tài khoản");
            intent.putExtra("recipientAccount", "VNPay");
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