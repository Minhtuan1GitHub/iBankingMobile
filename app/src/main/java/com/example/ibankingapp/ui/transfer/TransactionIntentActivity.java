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
import com.google.firebase.auth.FirebaseUser;
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
    private String recipientAccount; // Lưu STK người nhận
    private String recipientName;
    private String transferContent;

    private boolean vnpayPaymentSent = false;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private Customer currentCustomer;
    private String originalUid;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionIntentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            originalUid = firebaseUser.getUid();
        }

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        NotificationRepository notificationRepo = new NotificationRepository(AppDatabase.getInstance(this).notificationDao());
        notificationViewModel = new NotificationViewModel(notificationRepo);
        if (savedInstanceState != null) {
            // Khôi phục dữ liệu nếu Activity bị destroy và tạo lại
            transactionType = savedInstanceState.getString("transactionType");
            amountDouble = savedInstanceState.getDouble("amountDouble");
            recipientAccount = savedInstanceState.getString("recipientAccount");
            recipientName = savedInstanceState.getString("recipientName");
            transferContent = savedInstanceState.getString("transferContent");
            vnpayPaymentSent = savedInstanceState.getBoolean("vnpayPaymentSent");

            updateUI();
        } else {
            getIntentData();
        }

        loadCurrentCustomer();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnPay.setOnClickListener(v -> {
            if (vnpayPaymentSent) {
                navigateToSuccess();
            } else {
                startOtpProcess();
            }
        });
    }
    private void updateUI() {
        binding.tvAmountValue.setText(String.format("%,.0f VND", amountDouble));

        if ("TRANSFER".equals(transactionType)) {
            binding.tvTransactionType.setText("Chuyển khoản");
            binding.layoutRecipientName.setVisibility(View.VISIBLE);
            binding.layoutRecipientAccount.setVisibility(View.VISIBLE);
            binding.layoutContent.setVisibility(View.VISIBLE);

            binding.tvRecipientNameValue.setText(recipientName);
            binding.tvRecipientAccountValue.setText(recipientAccount);
            binding.tvContentValue.setText(transferContent);

        } else if ("DEPOSIT".equals(transactionType)) {
            binding.tvTransactionType.setText("Nạp tiền qua VNPay");
            binding.layoutRecipientName.setVisibility(View.GONE);
            binding.layoutRecipientAccount.setVisibility(View.GONE);
            binding.layoutContent.setVisibility(View.GONE);

        } else if ("WITHDRAW".equals(transactionType)) {
            binding.tvTransactionType.setText("Rút tiền về NH");
            binding.layoutRecipientName.setVisibility(View.VISIBLE);
            binding.tvRecipientNameValue.setText("Ngân hàng liên kết");
            binding.layoutRecipientAccount.setVisibility(View.GONE);
            binding.layoutContent.setVisibility(View.GONE);
        }
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
        recipientName = intent.getStringExtra("recipientName");
        recipientAccount = intent.getStringExtra("recipientAccount");
        transferContent = intent.getStringExtra("content");

        try {
            amountDouble = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            amountDouble = 0;
        }

        binding.tvAmountValue.setText(String.format("%,.0f VND", amountDouble));
        updateUI();
    }

    private void loadCurrentCustomer() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            finish(); return;
        }
        viewModel.getCustomer(firebaseUser.getUid()).observe(this, customer -> {
            if (customer != null){
                currentCustomer = customer;
                binding.tvSourceAccount.setText(customer.getAccountNumber());
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnPay.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnPay.setVisibility(View.VISIBLE);
        }
    }

    private void startOtpProcess() {
        if (currentCustomer == null) {
            Toast.makeText(this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            return;
        }
        String phoneNumber = "+84776750090";

        setLoading(true);
        Toast.makeText(this, "Đang gửi OTP...", Toast.LENGTH_SHORT).show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
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

    private void verifyOtpCode(String code) {
        if (mVerificationId == null) return;
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        verifyPhoneAuthCredential(credential);
    }

    private void verifyPhoneAuthCredential(PhoneAuthCredential credential) {
        Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
        proceedToTransaction();
    }

    private void proceedToTransaction() {
        if ("DEPOSIT".equals(transactionType)) {

            if (!vnpayPaymentSent) {
                setLoading(false);
                processDepositVNPay((int) amountDouble);
            } else {
                navigateToSuccess();
            }
        } else if ("TRANSFER".equals(transactionType)) {
            processTransferTransaction();
        } else {
            processInternalTransaction();
        }
    }

    private void processTransferTransaction() {
        final String uid = (originalUid != null) ? originalUid : mAuth.getCurrentUser().getUid();
        String fromAccount = currentCustomer.getAccountNumber();

        viewModel.transfer(fromAccount, recipientAccount, amountDouble).observe(this, success -> {
            setLoading(false);
            if (success != null && success) {
                String msg = "Chuyển " + String.format("%,.0f", amountDouble) + " VND đến " + recipientAccount;
                if (transferContent != null && !transferContent.isEmpty()) msg += ". ND: " + transferContent;

                createNotification(uid, "Chuyển tiền thành công", msg);
                navigateToSuccess();
            } else {
                Toast.makeText(this, "Giao dịch thất bại", Toast.LENGTH_LONG).show();
                binding.btnPay.setEnabled(true);
            }
        });
    }

    private void processInternalTransaction() {
        final String uid = (originalUid != null) ? originalUid : mAuth.getCurrentUser().getUid();

        viewModel.walletWithdraw(uid, amountDouble).observe(this, result -> {
            setLoading(false);
            if (result.isSuccess()) {
                createNotification(uid, "Rút tiền thành công", "Đã rút " + String.format("%,.0f", amountDouble) + " VND");
                navigateToSuccess();
            } else {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                binding.btnPay.setEnabled(true);
            }
        });
    }

    private void processDepositVNPay(int amount) {
        try {
            CreateOrder createOrder = new CreateOrder();
            String paymentUrl = createOrder.createOrder(String.valueOf(amount));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(browserIntent);
            vnpayPaymentSent = true;
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo link thanh toán", Toast.LENGTH_SHORT).show();
        }
    }

    private void processDepositSuccess() {
        final String uid = (originalUid != null) ? originalUid : mAuth.getCurrentUser().getUid();

        viewModel.walletDeposit(uid, amountDouble).observe(this, result -> {
            if (result.isSuccess()) {
                createNotification(uid, "Nạp tiền thành công", "Đã nạp " + String.format("%,.0f", amountDouble) + " VND qua VNPay");
                navigateToSuccess();
            } else {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                processDepositSuccess();
            } else {
                Toast.makeText(this, "Giao dịch thất bại/Hủy bỏ", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void createNotification(String customerId, String title, String message) {
        NotificationEntity notification = new NotificationEntity();
        notification.setCustomerId(customerId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTimestamp(System.currentTimeMillis());
        notification.setRead(false);
        notificationViewModel.addNotification(notification);
        NotificationHelper.send(this, title, message);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Lưu lại các biến quan trọng
        outState.putString("transactionType", transactionType);
        outState.putDouble("amountDouble", amountDouble);
        outState.putString("recipientAccount", recipientAccount);
        outState.putString("recipientName", recipientName);
        outState.putString("transferContent", transferContent);
        outState.putBoolean("vnpayPaymentSent", vnpayPaymentSent);
    }

    private void navigateToSuccess() {
        Intent successIntent = new Intent(this, SuccessfullTransferActivity.class);
        successIntent.putExtra("IS_SUCCESS", true);
        successIntent.putExtra("amount", amountDouble);
        successIntent.putExtra("to", recipientName);
        successIntent.putExtra("from", recipientAccount); // Hoặc lấy từ currentCustomer
        successIntent.putExtra("time", System.currentTimeMillis());
        successIntent.putExtra("transactionType", transactionType);

        String desc = "Chuyển khoản";
        if ("DEPOSIT".equals(transactionType)) desc = "Nạp tiền qua VNPay";
        else if ("WITHDRAW".equals(transactionType)) desc = "Rút tiền về ngân hàng";
        else if (transferContent != null && !transferContent.isEmpty()) desc = transferContent;

        successIntent.putExtra("description", desc);

        startActivity(successIntent);
        finish();
    }
}