package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.R;
import com.example.ibankingapp.databinding.FragmentWithdrawBinding;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.Locale;

public class WithdrawFragment extends Fragment {

    private FragmentWithdrawBinding binding;
    private CustomerViewModel viewModel;
    private double currentBalance = 0.0; // Biến lưu số dư hiện tại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentWithdrawBinding.inflate(inflater, container, false);


        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        loadCurrentBalance();

        setupChipGroup();

        binding.btnAction.setOnClickListener(v -> handleWithdraw());

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void loadCurrentBalance() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        viewModel.getCustomer(user.getUid()).observe(getViewLifecycleOwner(), customer -> {
            if (customer != null) {
                currentBalance = customer.getBalance();

                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                String balanceStr = formatter.format(currentBalance);

                binding.tvAvailableBalance.setText("Số dư khả dụng: " + balanceStr);
            }
        });
    }

    @SuppressLint("SetTextI18n")
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

            if (amount > currentBalance) {
                binding.tilMoney.setError("Số dư không đủ để thực hiện giao dịch");
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