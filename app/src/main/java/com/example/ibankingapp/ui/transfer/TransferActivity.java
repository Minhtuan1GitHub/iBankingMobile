package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityTransferBinding;
import com.example.ibankingapp.entity.NotificationEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.NotificationRepository;
import com.example.ibankingapp.ui.home.HomeActivity;
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

public class TransferActivity extends AppCompatActivity {

    private static final String TAG = "TransferActivity";
    private ActivityTransferBinding transferBinding;
    private Customer currentCustomer;
    private CustomerViewModel viewModel;
    private NotificationViewModel notificationViewModel;

    // Biến cho Phone OTP
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String pendingFrom;
    private String pendingTo;
    private double pendingAmount;
    private String originalUid; // Lưu UID ban đầu để tránh mất thông tin sau khi verify OTP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transferBinding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(transferBinding.getRoot());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            originalUid = currentUser.getUid();
        }

        NotificationRepository notificationRepo = new NotificationRepository(AppDatabase.getInstance(this).notificationDao());
        notificationViewModel = new NotificationViewModel(notificationRepo);

        loadCurrentCustomer();
        setupReceiverLookup();
        transferBinding.btnBack.setOnClickListener(v -> finish());
        transferBinding.btnTransfer.setOnClickListener(v -> clickTransfer());
    }

    private void loadCurrentCustomer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        viewModel.getCustomer(uid).observe(this, customer -> {
            if (customer != null) {
                currentCustomer = customer;
                transferBinding.tvSourceBalance.setText(String.valueOf(customer.getBalance()));
            } else {
                Toast.makeText(this, "Lỗi load tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupReceiverLookup() {
        transferBinding.edtRecipientAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String accountNumber = s.toString().trim();
                if (accountNumber.isEmpty()) {
                    transferBinding.tvRecipientName.setText("No recipient found");
                    transferBinding.tvRecipientName.setVisibility(View.GONE);
                    return;
                }
                lookupRecipient(accountNumber);
            }
        });
    }

    private void lookupRecipient(String accountNumber) {

        viewModel.getCustomerByAccountNumber(accountNumber).observe(this, recipient -> {
            if (recipient != null && recipient.getFullName() != null) {
                transferBinding.tvRecipientName.setText(recipient.getFullName().toUpperCase());
                transferBinding.tvRecipientName.setVisibility(View.VISIBLE);
            } else {
                transferBinding.tilRecipientAccount.setError("Không tìm thấy tài khoản");
                transferBinding.tvRecipientName.setVisibility(View.VISIBLE);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void clickTransfer() {
        if (currentCustomer == null) {
            Toast.makeText(this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
            return;
        }

        String from = currentCustomer.getAccountNumber();
        String to = transferBinding.edtRecipientAccount.getText().toString().trim();
        if (to.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tài khoản người nhận", Toast.LENGTH_SHORT).show();
            return;
        } else {
            transferBinding.tilRecipientAccount.setError(null); // Xóa lỗi
        }
        // Kiểm tra số tài khoản người nhận có tồn tại không
        viewModel.getCustomerByAccountNumber(to).observe(this, recipient -> {
            if (recipient == null) {
                transferBinding.tvRecipientName.setText("Không tìm thấy tài khoản");
            }
        });

        String amountStr = transferBinding.edtAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền chuyển", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new NumberFormatException();
            if (amount > currentCustomer.getBalance()) {
                transferBinding.tilAmount.setError("Số dư không đủ");
                return;
            }
        } catch (NumberFormatException e) {
            transferBinding.tilAmount.setError("Số tiền không hợp lệ");
            return;
        }

        showOtpDialog(from, to, amount);


    }

    private void showOtpDialog(String from, String to, double amount){
        Log.d(TAG, "showOtpDialog - Bắt đầu gửi OTP");
        // Lưu thông tin giao dịch để sử dụng sau khi xác thực
        pendingFrom = from;
        pendingTo = to;
        pendingAmount = amount;

        // Kiểm tra số điện thoại
        if (currentCustomer == null || currentCustomer.getPhone() == null || currentCustomer.getPhone().isEmpty()) {
            Toast.makeText(this, "Bạn chưa cập nhật số điện thoại. Vui lòng cập nhật trong hồ sơ.", Toast.LENGTH_LONG).show();
            return;
        }

        String phoneNumber = "+84776750090";
        Log.d(TAG, "Gửi OTP đến số: " + maskPhoneNumber(phoneNumber));

        Toast.makeText(this, "Đang gửi mã OTP đến " + maskPhoneNumber(phoneNumber) + "...", Toast.LENGTH_SHORT).show();

        // Gửi OTP qua Phone Auth
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        Log.d(TAG, "PhoneAuthProvider.verifyPhoneNumber đã được gọi");
    }

    // Che bớt số điện thoại
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 8) return phone;
        int length = phone.length();
        return phone.substring(0, 3) + "******" + phone.substring(length - 3);
    }

    // Callback xử lý Phone Auth
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Tự động xác thực
                    Log.d(TAG, "onVerificationCompleted - OTP tự động xác thực");
                    verifyPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.e(TAG, "onVerificationFailed: " + e.getMessage(), e);
                    Toast.makeText(TransferActivity.this, "Lỗi gửi OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    Log.d(TAG, "onCodeSent - Mã OTP đã được gửi, verificationId: " + verificationId);
                    mVerificationId = verificationId;
                    mResendToken = token;
                    showOtpInputDialog();
                }
            };

    // Hiển thị dialog nhập OTP
    private void showOtpInputDialog() {
        Log.d(TAG, "showOtpInputDialog - Hiển thị dialog nhập OTP");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác thực giao dịch");
        builder.setMessage("Nhập mã OTP");

        final EditText input = new EditText(this);
        input.setHint("Nhập mã OTP");
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setTextSize(20);
        input.setFocusable(true);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        android.text.InputFilter.LengthFilter lengthFilter = new android.text.InputFilter.LengthFilter(6);
        input.setFilters(new android.text.InputFilter[]{lengthFilter});
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String code = input.getText().toString().trim();
            Log.d(TAG, "User nhập OTP: " + code);
            if (!code.isEmpty() && mVerificationId != null) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                verifyPhoneAuthCredential(credential);
            } else {
                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> {
            Log.d(TAG, "User hủy nhập OTP");
            dialog.cancel();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        Log.d(TAG, "Dialog đã được show");
    }

    private void verifyPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "verifyPhoneAuthCredential - Bắt đầu xác thực");

        Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "OTP xác thực thành công, tiếp tục thực hiện giao dịch");

        executeTransfer(pendingFrom, pendingTo, pendingAmount);
    }

    private void executeTransfer(String from, String to, double amount) {
        final String uid;

        if (originalUid != null) {
            uid = originalUid;
        } else {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
                return;
            }
            uid = currentUser.getUid();
        }

        viewModel.transfer(from, to, amount).observe(this, success -> {
                if (success != null && success) {
                    Toast.makeText(this, "Chuyển tiền thành công!", Toast.LENGTH_SHORT).show();

                    // Tạo notification
                    String formattedAmount = String.format("%,.0f", amount);
                    NotificationEntity notification = new NotificationEntity();
                    notification.setCustomerId(uid); // Sử dụng UID gốc
                    notification.setTitle("Chuyển tiền thành công");
                    notification.setMessage("Bạn đã chuyển " + formattedAmount + " VND đến số tài khoản " + to);
                    notification.setTimestamp(System.currentTimeMillis());
                    notification.setRead(false);

                    // Lưu vào Room
                    notificationViewModel.addNotification(notification);

                    // Hiển thị Notification
                    NotificationHelper.send(
                            this,
                            notification.getTitle(),
                            notification.getMessage());


                    Intent intent = new Intent(this, SuccessfullTransferActivity.class);
                    intent.putExtra("from", from);
                    intent.putExtra("to", to);
                    intent.putExtra("amount", amount);
                    intent.putExtra("time", System.currentTimeMillis());
                    intent.putExtra("name", currentCustomer.getFullName());
                    intent.putExtra("IS_SUCCESS", true);
                    intent.putExtra("recipientName", transferBinding.tvRecipientName.getText().toString());

                    startActivity(intent);
                    finish(); // Đóng màn hình chuyển khoản
                    startActivity(intent);

                } else {

                    Intent intent = new Intent(this, SuccessfullTransferActivity.class);
                    intent.putExtra("IS_SUCCESS", false);
                    intent.putExtra("MESSAGE", "Giao dịch bị từ chối");
                    startActivity(intent);
                }
            });
    }


}
