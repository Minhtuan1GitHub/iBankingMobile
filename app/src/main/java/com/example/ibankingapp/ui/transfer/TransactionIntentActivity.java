package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.Api.CreateOrder; // Import class vừa tạo
import com.example.ibankingapp.databinding.ActivityTransactionIntentBinding;

public class TransactionIntentActivity extends AppCompatActivity {
    private ActivityTransactionIntentBinding binding;
    private String transactionType; // Cần nhận loại giao dịch: "DEPOSIT" hoặc "WITHDRAW"
    private double amountDouble;
    private boolean vnpayPaymentSent = false; // Đánh dấu đã gửi yêu cầu thanh toán VNPay

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionIntentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

    // --- Xử lý nội bộ (Rút tiền) ---
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

        // Lấy thông tin tài khoản từ Firestore
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("customers")
            .document(uid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(this, "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                    binding.btnPay.setEnabled(true);
                    return;
                }

                // Lấy số dư hiện tại
                Object balanceObj = documentSnapshot.get("balance");
                double currentBalance = 0;
                if (balanceObj instanceof Number) {
                    currentBalance = ((Number) balanceObj).doubleValue();
                }

                // Kiểm tra số dư
                if (currentBalance < amountDouble) {
                    Toast.makeText(this, "Số dư không đủ để thực hiện giao dịch", Toast.LENGTH_LONG).show();
                    binding.btnPay.setEnabled(true);
                    return;
                }

                // Thực hiện rút tiền
                double newBalance = currentBalance - amountDouble;

                // Cập nhật Firestore
                db.collection("customers")
                    .document(uid)
                    .update("balance", newBalance)
                    .addOnSuccessListener(aVoid -> {
                        // Lấy thông tin tài khoản
                        String accountNumber = documentSnapshot.getString("accountNumber");

                        // Cập nhật Room database (offline support)
                        updateLocalBalance(accountNumber, newBalance);

                        // Lưu lịch sử giao dịch
                        logWithdrawTransaction(accountNumber, amountDouble);

                        // Chuyển sang màn hình thành công
                        navigateToSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi thực hiện giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.btnPay.setEnabled(true);
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi lấy thông tin tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.btnPay.setEnabled(true);
            });
    }

    // Cập nhật số dư trong Room database
    private void updateLocalBalance(String accountNumber, double newBalance) {
        new Thread(() -> {
            com.example.ibankingapp.data.database.AppDatabase db =
                com.example.ibankingapp.data.database.AppDatabase.getInstance(this);
            com.example.ibankingapp.entity.CustomerEntity customer =
                db.customerDao().getCustomerByAccountNumber(accountNumber);

            if (customer != null) {
                customer.setBalance(newBalance);
                db.customerDao().updateCustomer(customer);
            }
        }).start();
    }

    // Lưu lịch sử giao dịch rút tiền
    private void logWithdrawTransaction(String accountNumber, double amount) {
        com.example.ibankingapp.data.database.AppDatabase db =
            com.example.ibankingapp.data.database.AppDatabase.getInstance(this);
        com.example.ibankingapp.repository.TransactionRepository transactionRepository =
            new com.example.ibankingapp.repository.TransactionRepository(db.transactionDao());

        transactionRepository.logTransaction(
            accountNumber,              // from: số tài khoản người rút
            "CASH_WITHDRAW",            // to: đích đến (rút tiền mặt)
            amount,                     // số tiền
            "success",                  // trạng thái
            "withdraw",                 // loại giao dịch
            "Rút tiền thành công"       // ghi chú
        );
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
    
    // Xử lý nạp tiền thành công từ VNPay
    private void processDepositSuccess() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String uid = currentUser.getUid();
        
        // Lấy thông tin tài khoản từ Firestore
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        db.collection("customers")
            .document(uid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(this, "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Lấy số dư hiện tại
                Object balanceObj = documentSnapshot.get("balance");
                double currentBalance = 0;
                if (balanceObj instanceof Number) {
                    currentBalance = ((Number) balanceObj).doubleValue();
                }
                
                // Cộng tiền vào tài khoản
                double newBalance = currentBalance + amountDouble;
                
                // Cập nhật Firestore
                db.collection("customers")
                    .document(uid)
                    .update("balance", newBalance)
                    .addOnSuccessListener(aVoid -> {
                        // Lấy thông tin tài khoản
                        String accountNumber = documentSnapshot.getString("accountNumber");
                        
                        // Cập nhật Room database (offline support)
                        updateLocalBalance(accountNumber, newBalance);
                        
                        // Lưu lịch sử giao dịch
                        logDepositTransaction(accountNumber, amountDouble);
                        
                        // Chuyển sang màn hình thành công
                        navigateToSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi cập nhật số dư: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi khi lấy thông tin tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    // Lưu lịch sử giao dịch nạp tiền
    private void logDepositTransaction(String accountNumber, double amount) {
        com.example.ibankingapp.data.database.AppDatabase db =
            com.example.ibankingapp.data.database.AppDatabase.getInstance(this);
        com.example.ibankingapp.repository.TransactionRepository transactionRepository =
            new com.example.ibankingapp.repository.TransactionRepository(db.transactionDao());
        
        transactionRepository.logTransaction(
            "VNPAY",                    // from: nguồn nạp tiền
            accountNumber,              // to: số tài khoản nhận
            amount,                     // số tiền
            "success",                  // trạng thái
            "deposit",                  // loại giao dịch
            "Nạp tiền qua VNPay thành công"  // ghi chú
        );
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