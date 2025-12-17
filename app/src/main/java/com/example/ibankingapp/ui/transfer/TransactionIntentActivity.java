package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.Api.CreateOrder;
import com.example.ibankingapp.databinding.ActivityTransactionIntentBinding;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;

public class TransactionIntentActivity extends AppCompatActivity {
    private ActivityTransactionIntentBinding binding;
    private CustomerViewModel viewModel;
    private String transactionType; // Cần nhận loại giao dịch: "DEPOSIT" hoặc "WITHDRAW"
    private double amountDouble;
    private boolean vnpayPaymentSent = false; // Đánh dấu đã gửi yêu cầu thanh toán VNPay

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionIntentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        // 1. Lấy dữ liệu từ Intent gửi sang
        Intent intent = getIntent();
        String amountStr = intent.getStringExtra("amount");
        if (amountStr != null) {
            amountStr = amountStr.replace("VND", "").replace(" ", "");
        } else {
            amountStr = "0";
        }
        transactionType = intent.getStringExtra("transactionType"); // Nhận loại giao dịch

        // Validation cơ bản
        try {
            amountDouble = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            amountDouble = 0;
        }

        // 2. Hiển thị lên giao diện
        binding.tvAmountValue.setText(amountStr + " VND");
        binding.tvRecipientNameValue.setText(intent.getStringExtra("recipientName"));
        binding.tvRecipientAccountValue.setText(intent.getStringExtra("recipientAccount"));

        // 3. Xử lý sự kiện nút Xác nhận (btnConfirm)
        // (Giả sử trong layout file xml của em có nút id là btnConfirm)
        binding.btnPay.setOnClickListener(v -> {
            if ("DEPOSIT".equals(transactionType)) {
                if (!vnpayPaymentSent) {
                    // -> Nạp tiền: Gọi VNPay
                    processDepositVNPay((int) amountDouble);
                } else {
                    // Người dùng xác nhận đã thanh toán xong -> Chuyển sang màn hình thành công
                    navigateToSuccess();
                }
            } else {
                // -> Rút tiền/Chuyển khoản: Xử lý nội bộ
                processInternalTransaction();
            }
        });

        // Nút Back
//        if (binding.btnBack != null) {
//            binding.btnBack.setOnClickListener(v -> finish());
//        }
    }

    // --- Xử lý gọi VNPay ---
    private void processDepositVNPay(int amount) {
        try {
            CreateOrder createOrder = new CreateOrder();
            // Hàm createOrder trả về URL thanh toán đầy đủ
            String paymentUrl = createOrder.createOrder(String.valueOf(amount));

            // Mở trình duyệt với URL này
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(browserIntent);
            vnpayPaymentSent = true;
//
//            // Đánh dấu đã gửi yêu cầu thanh toán
//
//            binding.btnPay.setText("XÁC NHẬN ĐÃ THANH TOÁN");
//
//            // Hiển thị thông báo hướng dẫn
//            Toast.makeText(this,
//                "Vui lòng hoàn tất thanh toán trên trang VNPay, sau đó quay lại app và nhấn nút xác nhận",
//                Toast.LENGTH_LONG).show();
//
//            // (Trong production, cần có webhook server để verify thanh toán)

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo link thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // --- Xử lý nội bộ (Rút tiền) - THEO MÔ HÌNH MVVM ---
    private void processInternalTransaction() {
        // Lấy thông tin người dùng hiện tại
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        // Vô hiệu hóa nút để tránh click nhiều lần
        binding.btnPay.setEnabled(false);

        // Gọi ViewModel để xử lý withdraw
        viewModel.walletWithdraw(uid, amountDouble).observe(this, result -> {
            if (result.isSuccess()) {
                // Giao dịch thành công
                navigateToSuccess();
            } else {
                // Giao dịch thất bại
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                binding.btnPay.setEnabled(true);
            }
        });
    }

    // --- Hứng kết quả trả về từ VNPay (Deep Link) ---
    @Override
    protected void onNewIntent(@androidx.annotation.NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handlePaymentResult(intent);
    }

    private void handlePaymentResult(Intent intent) {
        Uri data = intent.getData();
        // Kiểm tra xem có phải link callback từ VNPay không (ibanking://result)
        if (data != null && data.toString().startsWith("ibanking://result")) {
            String responseCode = data.getQueryParameter("vnp_ResponseCode");

            if ("00".equals(responseCode)) {
                // Thanh toán thành công -> Cập nhật số dư và chuyển sang màn hình thành công
                processDepositSuccess();
            } else {
                Toast.makeText(this, "Giao dịch thất bại/Hủy bỏ", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    // Xử lý nạp tiền thành công từ VNPay - THEO MÔ HÌNH MVVM
    private void processDepositSuccess() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        // Gọi ViewModel để xử lý deposit
        viewModel.walletDeposit(uid, amountDouble).observe(this, result -> {
            if (result.isSuccess()) {
                // Giao dịch thành công
                navigateToSuccess();
            } else {
                // Giao dịch thất bại
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToSuccess() {
        Intent successIntent = new Intent(this, SuccessfullTransferActivity.class);

        // Truyền đầy đủ thông tin giao dịch
        successIntent.putExtra("title", "GIAO DỊCH THÀNH CÔNG");
        successIntent.putExtra("amount", amountDouble);
        successIntent.putExtra("to", getIntent().getStringExtra("recipientName"));
        successIntent.putExtra("from", getIntent().getStringExtra("recipientAccount"));
        successIntent.putExtra("time", System.currentTimeMillis());
        successIntent.putExtra("transactionType", transactionType);

        // Thêm nội dung giao dịch dựa trên loại
        String description;
        if ("DEPOSIT".equals(transactionType)) {
            description = "Nạp tiền vào tài khoản qua VNPay";
        } else if ("WITHDRAW".equals(transactionType)) {
            description = "Rút tiền từ tài khoản";
        } else {
            description = "Chuyển khoản";
        }
        successIntent.putExtra("description", description);

        startActivity(successIntent);
        finish();
    }
}