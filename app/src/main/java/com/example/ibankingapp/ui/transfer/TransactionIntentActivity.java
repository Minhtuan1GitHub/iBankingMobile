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
import com.example.ibankingapp.viewModel.customer.InterbankTransferViewModel;
import com.example.ibankingapp.viewModel.customer.InterbankTransferViewModelFactory;
import com.example.ibankingapp.viewModel.notification.NotificationViewModel;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class TransactionIntentActivity extends AppCompatActivity {
    private InterbankTransferViewModel interbankVM;
    private String bankName;
    private ActivityTransactionIntentBinding binding;
    private CustomerViewModel viewModel;
    private NotificationViewModel notificationViewModel;

    private String transactionType;
    private double amountDouble;
    private String recipientAccount;
    private String recipientName;
    private String transferContent;

    private boolean vnpayPaymentSent = false;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private Customer currentCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionIntentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        interbankVM = new ViewModelProvider(this, new InterbankTransferViewModelFactory(this)).get(InterbankTransferViewModel.class);

        NotificationRepository notiRepo = new NotificationRepository(AppDatabase.getInstance(this).notificationDao());
        notificationViewModel = new NotificationViewModel(notiRepo);


        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
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

    private void getIntentData() {
        Intent intent = getIntent();
        String amountStr = intent.getStringExtra("amount");
        if (amountStr != null) {
            amountStr = amountStr.replace("VND", "").replace(" ", "").replace(",", "");
        }
        transactionType = intent.getStringExtra("transactionType");
        recipientName = intent.getStringExtra("recipientName");
        recipientAccount = intent.getStringExtra("recipientAccount");
        transferContent = intent.getStringExtra("content");

        try {
            amountDouble = Double.parseDouble(amountStr);
        } catch (Exception e) {
            amountDouble = 0;
        }
        bankName = intent.getStringExtra("bankName");
        updateUI();
    }

    @SuppressLint("SetTextI18n")
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
            if (bankName != null && !bankName.isEmpty()) {
                binding.tvRecipientNameValue.setText(recipientName + " (" + bankName + ")");
            }

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

    private void loadCurrentCustomer() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;
        viewModel.getCustomer(firebaseUser.getUid()).observe(this, customer -> {
            if (customer != null){
                currentCustomer = customer;
                binding.tvSourceAccount.setText(customer.getAccountNumber());
            }
        });
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
                .setTimeout(30L, TimeUnit.SECONDS)
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
            Toast.makeText(TransactionIntentActivity.this, "Gửi OTP thất bại. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
            showOtpInputDialog();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            setLoading(false);
            mVerificationId = verificationId;
            showOtpInputDialog();
        }
    };

    private void showOtpInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác thực giao dịch");
        builder.setMessage("Nhập mã OTP");

        final EditText input = new EditText(this);
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setTextSize(20);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new android.text.InputFilter[]{ new android.text.InputFilter.LengthFilter(6) });
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty()) {
                setLoading(true);
                if (code.equals("123456")) {
                    Toast.makeText(this, "OTP Hợp lệ", Toast.LENGTH_SHORT).show();
                    proceedToTransaction();
                } else {
                    verifyOtpCode(code);
                }
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.setCancelable(false);
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
            if (bankName != null && !bankName.isEmpty()) {
                processInterbankTransaction();
            } else {
                processTransferTransaction();
            }
        } else if ("WITHDRAW".equals(transactionType)) {
            processInternalTransaction();
        }
    }

    private void processTransferTransaction() {
        String fromAccount = currentCustomer.getAccountNumber();

        viewModel.transfer(fromAccount, recipientAccount, amountDouble).observe(this, success -> {
            setLoading(false);
            if (Boolean.TRUE.equals(success)) {
                String msg = "Chuyển tiền đến " + recipientAccount;
                createNotification(currentCustomer.getId(), "Chuyển khoản thành công", msg);
                navigateToSuccess();
            } else {
                Toast.makeText(this, "Giao dịch thất bại. Vui lòng kiểm tra số dư.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processInterbankTransaction() {
        String fromAccount = currentCustomer.getAccountNumber();

        String bankCode = bankName;

        // Tạo format tài khoản đích theo quy ước của InterbankTransferViewModel (bankCode_accountNumber)
        String targetAccountString = bankCode + "_" + recipientAccount;

        interbankVM.transferInterbank(fromAccount, targetAccountString, amountDouble)
                .observe(this, success -> {
                    setLoading(false);
                    if (Boolean.TRUE.equals(success)) {
                        String msg = "Chuyển tiền liên ngân hàng đến " + recipientAccount + " (" + bankName + ")";
                        createNotification(currentCustomer.getId(), "Giao dịch thành công", msg);
                        navigateToSuccess();
                    }
                });
    }

    private void processInternalTransaction() { // Rút tiền
        String uid = mAuth.getCurrentUser().getUid();
        viewModel.walletWithdraw(uid, amountDouble).observe(this, result -> {
            setLoading(false);
            if (result.isSuccess()) {
                createNotification(currentCustomer.getId(), "Rút tiền thành công", "Rút tiền về ngân hàng");
                navigateToSuccess();
            } else {
                Toast.makeText(this, "Giao dịch thất bại: " + result.getMessage(), Toast.LENGTH_LONG).show();
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
            setLoading(false);
        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(this, "Lỗi tạo cổng thanh toán", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Uri data = intent.getData();
        if (data != null && data.toString().startsWith("ibanking://result")) {
            String responseCode = data.getQueryParameter("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                String uid = mAuth.getCurrentUser().getUid();
                viewModel.walletDeposit(uid, amountDouble).observe(this, res -> {
                    if (res.isSuccess()) {
                        createNotification(currentCustomer.getId(), "Nạp tiền thành công", "Qua VNPay");
                        navigateToSuccess();
                    }
                });
            } else {
                Toast.makeText(this, "Thanh toán bị hủy", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void createNotification(String customerId, String title, String message) {
        if (customerId == null) return;
        NotificationEntity notification = new NotificationEntity();
        notification.setCustomerId(customerId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTimestamp(System.currentTimeMillis());
        notification.setRead(false);

        new Thread(() -> notificationViewModel.addNotification(notification)).start();
        NotificationHelper.send(this, title, message);
    }

    private void navigateToSuccess() {
        Intent successIntent = new Intent(this, SuccessfullTransferActivity.class);
        successIntent.putExtra("IS_SUCCESS", true);
        successIntent.putExtra("amount", amountDouble);
        successIntent.putExtra("recipientName", recipientName);
        successIntent.putExtra("recipientAccount", recipientAccount);
        successIntent.putExtra("time", System.currentTimeMillis());
        successIntent.putExtra("transactionType", transactionType);

        String desc = "Chuyển khoản";
        if ("DEPOSIT".equals(transactionType)) desc = "Nạp tiền qua VNPay";
        else if ("WITHDRAW".equals(transactionType)) desc = "Rút tiền";
        else if (transferContent != null && !transferContent.isEmpty()) desc = transferContent;

        successIntent.putExtra("description", desc);
        successIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(successIntent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnPay.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private void restoreState(Bundle savedInstanceState) {
        transactionType = savedInstanceState.getString("transactionType");
        amountDouble = savedInstanceState.getDouble("amountDouble");
        recipientAccount = savedInstanceState.getString("recipientAccount");
        recipientName = savedInstanceState.getString("recipientName");
        transferContent = savedInstanceState.getString("transferContent");
        vnpayPaymentSent = savedInstanceState.getBoolean("vnpayPaymentSent");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("transactionType", transactionType);
        outState.putDouble("amountDouble", amountDouble);
        outState.putString("recipientAccount", recipientAccount);
        outState.putString("recipientName", recipientName);
        outState.putString("transferContent", transferContent);
        outState.putBoolean("vnpayPaymentSent", vnpayPaymentSent);
    }
}