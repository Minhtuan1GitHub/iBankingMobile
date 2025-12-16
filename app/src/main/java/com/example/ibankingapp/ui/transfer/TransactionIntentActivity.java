package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.Api.CreateOrder; // Import class vừa tạo
import com.example.ibankingapp.databinding.ActivityTransactionIntentBinding;
import com.example.ibankingapp.ui.transfer.SuccessfullTransferActivity;

public class TransactionIntentActivity extends AppCompatActivity {
    private ActivityTransactionIntentBinding binding;
    private String transactionType; // Cần nhận loại giao dịch: "DEPOSIT" hoặc "WITHDRAW"
    private double amountDouble;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionIntentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Lấy dữ liệu từ Intent gửi sang
        Intent intent = getIntent();
        String amountStr = intent.getStringExtra("amount").replace("VND", "").replace(" ", ""); // Chuỗi số tiền
        transactionType = intent.getStringExtra("transactionType"); // Nhận loại giao dịch

        // Validation cơ bản
        if(amountStr == null) amountStr = "0";
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
                // -> Nạp tiền: Gọi VNPay
                processDepositVNPay((int) amountDouble);
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

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo link thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // --- Xử lý nội bộ (Rút tiền) ---
    private void processInternalTransaction() {
        // TODO: Gọi API trừ tiền trong DB tại đây
        // Giả lập thành công:
        navigateToSuccess();
    }

    // --- Hứng kết quả trả về từ VNPay (Deep Link) ---
    @Override
    protected void onNewIntent(Intent intent) {
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
                // Thanh toán thành công -> Chuyển sang màn hình Thành công
                navigateToSuccess();
            } else {
                Toast.makeText(this, "Giao dịch thất bại/Hủy bỏ", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void navigateToSuccess() {
        Intent successIntent = new Intent(this, SuccessfullTransferActivity.class);
        successIntent.putExtra("amount", String.valueOf((int)amountDouble));
        startActivity(successIntent);
        finish();
    }
}