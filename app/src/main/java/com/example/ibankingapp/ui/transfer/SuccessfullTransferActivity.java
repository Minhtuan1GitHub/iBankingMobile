package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.R;
import com.example.ibankingapp.databinding.ActivitySuccessfullTransferBinding;
import com.example.ibankingapp.ui.home.HomeActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class SuccessfullTransferActivity extends AppCompatActivity {
    private ActivitySuccessfullTransferBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessfullTransferBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean isSuccess = getIntent().getBooleanExtra("IS_SUCCESS", true);
        String message = getIntent().getStringExtra("MESSAGE");

        String recipientName = getIntent().getStringExtra("recipientName");
        String toAccount = getIntent().getStringExtra("recipientAccount");
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        long time = getIntent().getLongExtra("time", System.currentTimeMillis());
        String description = getIntent().getStringExtra("description");

        setupStatusUI(isSuccess, message);

        displayReceiptInfo(amount, time, recipientName, toAccount, description);

        setupButtons(isSuccess);
    }

    private void setupStatusUI(boolean isSuccess, String message) {
        if (isSuccess) {
            binding.ivStatusIcon.setImageResource(R.drawable.ic_check);
            binding.ivStatusIcon.setColorFilter(Color.parseColor("#4CAF50"));
            binding.ivStatusIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));

            binding.tvStatusTitle.setText("Giao dịch thành công!");
            binding.tvFailureReason.setVisibility(View.GONE);
            binding.cardReceipt.setVisibility(View.VISIBLE); // Hiện biên lai

            binding.btnNewTransaction.setText("GIAO DỊCH MỚI");

        } else {

            binding.ivStatusIcon.setImageResource(R.drawable.ic_close);
            binding.ivStatusIcon.setColorFilter(Color.parseColor("#F44336")); // Đỏ
            binding.ivStatusIcon.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));

            binding.tvStatusTitle.setText("Giao dịch thất bại");

            // Hiện lý do lỗi
            binding.tvFailureReason.setVisibility(View.VISIBLE);
            binding.tvFailureReason.setText(message != null ? message : "Đã có lỗi xảy ra");

            // ẩn biên lai nếu thất bại
            binding.cardReceipt.setVisibility(View.GONE);
            binding.btnNewTransaction.setText("THỬ LẠI");
        }
    }

    private void displayReceiptInfo(double amount, long time, String name, String account, String desc) {
        // tiền
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(amount);

        //thời gian
        String formattedTime = android.text.format.DateFormat.format("HH:mm - dd/MM/yyyy", time).toString();

        // Gán dữ liệu vào View
        binding.tvAmountValue.setText(formattedAmount);
        binding.tvTimeValue.setText(formattedTime);

        // Tên người nhận
        if (name != null && !name.isEmpty()) {
            binding.tvRecipientNameValue.setText(name);
        } else {
            binding.tvRecipientNameValue.setText("N/A");
        }

        // Số tài khoản nhận
        if (account != null && !account.isEmpty()) {
            binding.tvRecipientAccountValue.setText(account);
        } else {
            binding.tvRecipientAccountValue.setText("N/A");
        }

        // Nội dung
        if (desc != null && !desc.isEmpty()) {
            binding.tvDescValue.setText(desc);
        } else {
            binding.tvDescValue.setText("Chuyển tiền");
        }
    }

    private void setupButtons(boolean isSuccess) {
        // Nút Về trang chủ
        binding.btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Nút Giao dịch mới / Thử lại
        binding.btnNewTransaction.setOnClickListener(v -> {
            if (isSuccess) {
                String type = getIntent().getStringExtra("transactionType");
                if (type == null) type = "TRANSFER";

                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Gửi tín hiệu để HomeActivity biết cần mở Fragment nào
                if (type.equals("DEPOSIT")) {
                    intent.putExtra("NAVIGATE_TO", "DEPOSIT");
                } else if (type.equals("WITHDRAW")) {
                    intent.putExtra("NAVIGATE_TO", "WITHDRAW");
                } else {
                    intent.putExtra("NAVIGATE_TO", "TRANSFER");
                }

                startActivity(intent);
                finish();

            } else {
                finish();
            }
        });
    }
}