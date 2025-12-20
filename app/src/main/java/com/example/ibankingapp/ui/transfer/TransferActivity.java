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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TransferActivity extends AppCompatActivity {

    private ActivityTransferBinding transferBinding;
    private Customer currentCustomer;
    private CustomerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transferBinding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(transferBinding.getRoot());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        transferBinding.fabHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });

        loadCurrentCustomer();
        setupReceiverLookup();
        transferBinding.btnTransfer.setOnClickListener(v -> clickTransfer());
        transferBinding.tabTransferType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    // Chế độ: Chuyển tiền LIÊN NGÂN HÀNG
                    transferBinding.tilBankProvider.setVisibility(View.VISIBLE);
                    transferBinding.edtRecipientAccount.setHint("Số tài khoản liên ngân hàng");

                    // Nếu bạn muốn làm trống các ô nhập khi đổi tab để tránh nhầm lẫn
                    clearFields();
                } else {
                    // Chế độ: Chuyển tiền NỘI BỘ
                    transferBinding.tilBankProvider.setVisibility(View.GONE);
                    transferBinding.edtRecipientAccount.setHint("Số tài khoản/SĐT nội bộ");

                    clearFields();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });
    }
    private void clearFields(){}

    private void loadCurrentCustomer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentCustomer = doc.toObject(Customer.class);
                        if (currentCustomer != null) {
                            transferBinding.tvSourceBalance.setText(String.valueOf(currentCustomer.getBalance()));
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi load tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("customers")
                .whereEqualTo("accountNumber", accountNumber)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Customer recipient = query.getDocuments().get(0).toObject(Customer.class);
                        if (recipient != null && recipient.getFullName() != null) {
                            transferBinding.tvRecipientName.setText(recipient.getFullName());
                            transferBinding.tvRecipientName.setVisibility(View.VISIBLE);
                        } else {
                            transferBinding.tvRecipientName.setText("No recipient found");
                            transferBinding.tvRecipientName.setVisibility(View.VISIBLE);
                        }
                    } else {
                        transferBinding.tvRecipientName.setText("No recipient found");
                        transferBinding.tvRecipientName.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    transferBinding.tvRecipientName.setText("Error: " + e.getMessage());
                    transferBinding.tvRecipientName.setVisibility(View.VISIBLE);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập mã OTP");

        final EditText input = new EditText(this);
        input.setHint("Mã OTP");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String otp = input.getText().toString().trim();

            viewModel.verifyPin(uid, otp)
                    .observe(this, success -> {
                        if (Boolean.TRUE.equals(success)) {
                            executeTransfer(from, to, amount);
                        } else {
                            Toast.makeText(this, "OTP không đúng", Toast.LENGTH_SHORT).show();
                        }
                    });

        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void executeTransfer(String from, String to, double amount) {
        viewModel.transfer(from, to, amount).observe(this, success -> {
                if (success != null && success) {
                    Toast.makeText(this, "Chuyển tiền thành công!", Toast.LENGTH_SHORT).show();
                    NotificationEntity notification = new NotificationEntity();
                    notification.setCustomerId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    notification.setTitle("Chuyển tiền thành công");
                    notification.setMessage("Bạn đã chuyển " + amount + "đ đến số tài khoản " + to);
                    notification.setTimestamp(System.currentTimeMillis());
                    notification.setRead(false); // chưa đọc

                    // Lưu vào Room
                    NotificationRepository repo = new NotificationRepository(AppDatabase.getInstance(this).notificationDao());
                    repo.addNotification(notification);

                    // Hiển thị Notification Android
                    NotificationHelper.send(
                            this,
                            notification.getTitle(),
                            notification.getMessage());


                    //startActivity(new Intent(this, SuccessfullTransferActivity.class));
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
