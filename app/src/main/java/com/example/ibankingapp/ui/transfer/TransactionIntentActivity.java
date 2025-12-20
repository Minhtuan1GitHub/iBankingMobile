package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.Api.CreateOrder;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityTransactionIntentBinding;
import com.example.ibankingapp.entity.NotificationEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.NotificationRepository;
import com.example.ibankingapp.utils.NotificationHelper;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.example.ibankingapp.viewModel.notification.NotificationViewModel;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;


import java.util.concurrent.TimeUnit;

public class TransactionIntentActivity extends AppCompatActivity {
    private ActivityTransactionIntentBinding binding;
    private CustomerViewModel viewModel;
    private NotificationViewModel notificationViewModel;
    private String transactionType;
    private double amountDouble;
    private boolean vnpayPaymentSent = false;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private Customer currentCustomer;
    private String originalUid; // Lưu UID ban đầu

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionIntentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        // Lưu UID ban đầu
        com.google.firebase.auth.FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            originalUid = firebaseUser.getUid();
        }

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        NotificationRepository notificationRepo = new NotificationRepository(AppDatabase.getInstance(this).notificationDao());
        notificationViewModel = new NotificationViewModel(notificationRepo);

        // Load thông tin khách hàng hiện tại
        loadCurrentCustomer();

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

        binding.tvAmountValue.setText(amountStr + " VND");
        binding.tvRecipientNameValue.setText(intent.getStringExtra("recipientName"));
        binding.tvRecipientAccountValue.setText(intent.getStringExtra("recipientAccount"));

        binding.btnPay.setOnClickListener(v -> {
            if (vnpayPaymentSent) {
                // Người dùng đã thanh toán VNPay, xác nhận hoàn tất
                navigateToSuccess();
            } else {
                // Bắt đầu xác thực OTP trước khi giao dịch
                startOtpProcess();
            }
        });
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void getIntentData() {
        Intent intent = getIntent();
        String amountStr = intent.getStringExtra("amount");
        if (amountStr != null) {
            amountStr = amountStr.replace("VND", "").replace(" ", "");
        } else {
            amountStr = "0";
        }
        transactionType = intent.getStringExtra("transactionType");

        try {
            amountDouble = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            amountDouble = 0;
        }

        binding.tvAmountValue.setText(String.format("%,.0f VND", amountDouble));
        binding.tvRecipientNameValue.setText(intent.getStringExtra("recipientName"));
        binding.tvRecipientAccountValue.setText(intent.getStringExtra("recipientAccount"));
    }

    // Load thông tin khách hàng hiện tại
    private void loadCurrentCustomer() {
        com.google.firebase.auth.FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = firebaseUser.getUid();

        viewModel.getCustomer(uid).observe(this, customer -> {
            if (customer != null) {
                currentCustomer = customer;
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin khách hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnPay.setVisibility(View.INVISIBLE); // Ẩn nút đi
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnPay.setVisibility(View.VISIBLE);
        }
    }
    // Hàm gửi OTP
    private void startOtpProcess() {
        // Kiểm tra đã load thông tin khách hàng chưa
        if (currentCustomer == null) {
            Toast.makeText(this, "Đang tải thông tin, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = "+84776750090";//currentCustomer.getPhone();

        // Khóa nút để tránh bấm nhiều lần
        binding.btnPay.setEnabled(false);
        Toast.makeText(this, "Đang gửi mã OTP đến " + maskPhoneNumber(phoneNumber) + "...", Toast.LENGTH_SHORT).show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 8) return phone;
        int length = phone.length();
        return phone.substring(0, 3) + "******" + phone.substring(length - 3);
    }
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            setLoading(false);
            verifyPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            setLoading(false);
            Toast.makeText(TransactionIntentActivity.this, "Lỗi OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            setLoading(false);
            mVerificationId = verificationId;
            showOtpInputDialog();
        }
    };

    // Hiển thị Dialog nhập mã
    private void showOtpInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác thực giao dịch");
        builder.setMessage("Nhập mã OTP");

        final EditText input = new EditText(this);
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setTextSize(20);
        input.setFocusable(true);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        android.text.InputFilter.LengthFilter lengthFilter = new android.text.InputFilter.LengthFilter(6);
        input.setFilters(new android.text.InputFilter[]{lengthFilter});
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                setLoading(true);
                verifyOtpCode(code);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    // Kiểm tra mã nhập vào
    private void verifyOtpCode(String code) {
        if (mVerificationId == null) return;
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        verifyPhoneAuthCredential(credential);
    }

    private void verifyPhoneAuthCredential(PhoneAuthCredential credential) {

        Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();

        //xử lý giao dịch với UID gốc
        proceedToTransaction();
    }

    // Xử lý gọi VNPay
    private void processDepositVNPay(int amount) {
        try {
            CreateOrder createOrder = new CreateOrder();
            // Hàm createOrder trả về URL thanh toán đầy đủ
            String paymentUrl = createOrder.createOrder(String.valueOf(amount));

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(browserIntent);
            vnpayPaymentSent = true;
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo link thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void proceedToTransaction() {
        if ("DEPOSIT".equals(transactionType)) {

            if (!vnpayPaymentSent) {
                processDepositVNPay((int) amountDouble);
            } else {
                navigateToSuccess();
            }
        } else {
            processInternalTransaction();
        }
    }

    // Xử lý Rút tiền
    private void processInternalTransaction() {
        // Sử dụng UID gốc
        final String uid;

        if (originalUid != null) {
            uid = originalUid;
        } else {
            com.google.firebase.auth.FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                return;
            }
            uid = currentUser.getUid();
        }

        // Vô hiệu hóa nút để tránh click nhiều lần
        binding.btnPay.setEnabled(false);

        // Gọi ViewModel để xử lý withdraw
        viewModel.walletWithdraw(uid, amountDouble).observe(this, result -> {
            setLoading(false);
            if (result.isSuccess()) {
                // Tạo notification cho rút tiền thành công
                createNotification(
                    uid,
                    "Rút tiền thành công",
                    "Bạn đã rút " + String.format("%,.0f", amountDouble) + " VND từ tài khoản"
                );

                // Giao dịch thành công
                navigateToSuccess();
            } else {
                // Giao dịch thất bại
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                binding.btnPay.setEnabled(true);
            }
        });
    }

    // Hứng kết quả trả về từ VNPay
    @Override
    protected void onNewIntent(@androidx.annotation.NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handlePaymentResult(intent);
    }

    private void handlePaymentResult(Intent intent) {
        Uri data = intent.getData();

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
        final String uid;

        if (originalUid != null) {
            uid = originalUid;
        } else {
            com.google.firebase.auth.FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                return;
            }
            uid = currentUser.getUid();
        }

        // Gọi ViewModel để xử lý deposit
        viewModel.walletDeposit(uid, amountDouble).observe(this, result -> {
            if (result.isSuccess()) {
                // Tạo notification cho nạp tiền thành công
                createNotification(
                    uid,
                    "Nạp tiền thành công",
                    "Bạn đã nạp " + String.format("%,.0f", amountDouble) + " VND vào tài khoản qua VNPay"
                );

                // Giao dịch thành công
                navigateToSuccess();
            } else {
                // Giao dịch thất bại
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tạo notification cho giao dịch
    private void createNotification(String customerId, String title, String message) {
        // Tạo notification entity
        NotificationEntity notification = new NotificationEntity();
        notification.setCustomerId(customerId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTimestamp(System.currentTimeMillis());
        notification.setRead(false);

        // Lưu vào Room DB
        notificationViewModel.addNotification(notification);

        // Hiển thị notification Android (system notification)
        NotificationHelper.send(this, title, message);
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