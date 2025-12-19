package com.example.ibankingapp.ui.transfer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class TransferActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transferBinding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(transferBinding.getRoot());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        // Khởi tạo NotificationViewModel (tuân thủ MVVM)
        NotificationRepository notificationRepo = new NotificationRepository(AppDatabase.getInstance(this).notificationDao());
        notificationViewModel = new NotificationViewModel(notificationRepo);

        transferBinding.fabHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });

        loadCurrentCustomer();
        setupReceiverLookup();
        transferBinding.btnTransfer.setOnClickListener(v -> clickTransfer());
    }

    private void loadCurrentCustomer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // Sử dụng ViewModel để lấy dữ liệu (tuân thủ MVVM)
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
        // Sử dụng ViewModel để tìm kiếm (tuân thủ MVVM)
        viewModel.getCustomerByAccountNumber(accountNumber).observe(this, recipient -> {
            if (recipient != null && recipient.getFullName() != null) {
                transferBinding.tvRecipientName.setText(recipient.getFullName());
                transferBinding.tvRecipientName.setVisibility(View.VISIBLE);
            } else {
                transferBinding.tvRecipientName.setText("No recipient found");
                transferBinding.tvRecipientName.setVisibility(View.VISIBLE);
            }
        });
    }

    private void clickTransfer() {
        if (currentCustomer == null) {
            Toast.makeText(this, "Đang load dữ liệu tài khoản nguồn, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String from = currentCustomer.getAccountNumber();
        String to = transferBinding.edtRecipientAccount.getText().toString().trim();
        if (to.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tài khoản người nhận", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = transferBinding.edtAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền chuyển", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        showOtpDialog(from, to, amount);


    }

    private void showOtpDialog(String from, String to, double amount){
        // Lưu thông tin giao dịch để sử dụng sau khi xác thực
        pendingFrom = from;
        pendingTo = to;
        pendingAmount = amount;

        // Kiểm tra số điện thoại
        if (currentCustomer == null || currentCustomer.getPhone() == null || currentCustomer.getPhone().isEmpty()) {
            Toast.makeText(this, "Bạn chưa cập nhật số điện thoại. Vui lòng cập nhật trong hồ sơ.", Toast.LENGTH_LONG).show();
            return;
        }

        String phoneNumber = currentCustomer.getPhone();

        // Đảm bảo số điện thoại có định dạng quốc tế (+84...)
        if (!phoneNumber.startsWith("+")) {
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "+84" + phoneNumber.substring(1);
            } else {
                phoneNumber = "+84" + phoneNumber;
            }
        }

        Toast.makeText(this, "Đang gửi mã OTP đến " + maskPhoneNumber(phoneNumber) + "...", Toast.LENGTH_SHORT).show();

        // Gửi OTP qua Phone Auth
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
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
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(TransferActivity.this, "Lỗi gửi OTP: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    mVerificationId = verificationId;
                    mResendToken = token;
                    showOtpInputDialog();
                }
            };

    // Hiển thị dialog nhập OTP
    private void showOtpInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác thực giao dịch");
        builder.setMessage("Nhập mã OTP đã gửi đến số điện thoại của bạn");

        final EditText input = new EditText(this);
        input.setHint("Mã OTP");
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String code = input.getText().toString().trim();
            if (!code.isEmpty() && mVerificationId != null) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);
            } else {
                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Xác thực với Firebase
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                        // Thực hiện giao dịch
                        executeTransfer(pendingFrom, pendingTo, pendingAmount);
                    } else {
                        String msg = "Mã OTP không đúng";
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            msg = "Mã OTP không hợp lệ";
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void executeTransfer(String from, String to, double amount) {
        viewModel.transfer(from, to, amount).observe(this, success -> {
                if (success != null && success) {
                    Toast.makeText(this, "Chuyển tiền thành công!", Toast.LENGTH_SHORT).show();

                    // Tạo notification
                    NotificationEntity notification = new NotificationEntity();
                    notification.setCustomerId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    notification.setTitle("Chuyển tiền thành công");
                    notification.setMessage("Bạn đã chuyển " + amount + "đ đến số tài khoản " + to);
                    notification.setTimestamp(System.currentTimeMillis());
                    notification.setRead(false);

                    // Lưu vào Room qua ViewModel (tuân thủ MVVM)
                    notificationViewModel.addNotification(notification);

                    // Hiển thị Notification Android
                    NotificationHelper.send(
                            this,
                            notification.getTitle(),
                            notification.getMessage());

                    // Chuyển sang màn hình thành công
                    Intent intent = new Intent(this, SuccessfullTransferActivity.class);
                    intent.putExtra("from", from);
                    intent.putExtra("to", to);
                    intent.putExtra("amount", amount);
                    intent.putExtra("time", System.currentTimeMillis());
                    intent.putExtra("name", currentCustomer.getFullName());
                    startActivity(intent);

                } else {
                    Toast.makeText(this, "Chuyển tiền thất bại!", Toast.LENGTH_SHORT).show();
                }
            });
    }


}
