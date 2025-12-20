package com.example.ibankingapp.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.R;
import com.example.ibankingapp.databinding.ActivityHomeBinding;
import com.example.ibankingapp.ui.account.saving.SavingAccountActivity;

import com.example.ibankingapp.ui.login.LoginActivity;
import com.example.ibankingapp.ui.maps.MapsActivity;
import com.example.ibankingapp.ui.notification.NotificationActivity;
import com.example.ibankingapp.ui.setting.SettingActivity;
import com.example.ibankingapp.ui.transfer.DepositWithdrawActivity;
import com.example.ibankingapp.ui.transfer.TransferActivity;
import com.example.ibankingapp.ui.transfer.transaction.BillPaymentActivity;
import com.example.ibankingapp.ui.transfer.transaction.HistoryTransactionActivity;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.example.ibankingapp.viewModel.notification.NotificationViewModel;
import com.example.ibankingapp.viewModel.notification.NotificationViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;


public class HomeActivity extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    private ActivityHomeBinding homeBinding;
    private CustomerViewModel customerViewModel;
    private boolean isBalanceVisible = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Yêu cầu quyền notification cho Android 13+ (API 33+)
        requestNotificationPermission();

        // Khởi tạo CustomerViewModel
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        customerViewModel.getImage(uid).observe(this, image -> {
            if (image != null) {
                homeBinding.imgAvatar.setImageURI(Uri.parse(image));
            } else {
                homeBinding.imgAvatar.setImageResource(R.drawable.ic_saving);
            }
        });
        // Load thông tin người dùng
        loadUserInfo();

        // Setup các button listeners
        setupClickListeners();

        // Setup notification badge
        setupNotificationBadge();
    }

    /**
     * Load thông tin người dùng từ Firebase
     */
    private void loadUserInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = currentUser.getUid();

        // Lấy thông tin customer
        customerViewModel.getCustomer(uid).observe(this, customer -> {
            if (customer != null) {
                // Hiển thị tên người dùng
                String fullName = customer.getFullName();
                if (fullName != null && !fullName.isEmpty()) {
                    homeBinding.tvUsername.setText(fullName);
                } else {
                    homeBinding.tvUsername.setText("Khách hàng");
                }

                // Hiển thị số dư
                double balance = customer.getBalance();
                updateBalanceDisplay(balance);

                // Có thể hiển thị thêm số tài khoản nếu cần
                // String accountNumber = customer.getAccountNumber();
            }
        });

        // Toggle hiển thị/ẩn số dư
        homeBinding.imgEyes.setOnClickListener(v -> {
            isBalanceVisible = !isBalanceVisible;

            if (isBalanceVisible) {
                homeBinding.imgEyes.setImageResource(com.example.ibankingapp.R.drawable.ic_visibility_off);
                // Hiển thị số dư
                customerViewModel.getCustomer(uid).observe(this, customer -> {
                    if (customer != null) {
                        updateBalanceDisplay(customer.getBalance());
                    }
                });
            } else {
                homeBinding.imgEyes.setImageResource(com.example.ibankingapp.R.drawable.ic_visibility);
                // Ẩn số dư
                homeBinding.tvBalance.setText("*************");
            }
        });
    }

    /**
     * Cập nhật hiển thị số dư với format tiền tệ
     */
    private void updateBalanceDisplay(double balance) {
        if (isBalanceVisible) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedBalance = formatter.format(balance);
            homeBinding.tvBalance.setText(formattedBalance);
        } else {
            homeBinding.tvBalance.setText("******");
        }
    }

    /**
     * Setup các click listeners cho buttons
     */
    private void setupClickListeners() {
        homeBinding.fabTransfers.setOnClickListener(v -> {
            startActivity(new Intent(this, TransferActivity.class));
        });

        homeBinding.navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingActivity.class));

        });

        homeBinding.navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryTransactionActivity.class));
        });

        homeBinding.btnDepositWithdraw.setOnClickListener(v -> {
            Intent intent = new Intent(this, DepositWithdrawActivity.class);
            intent.putExtra("tab", 0); // 0 = Nạp
            startActivity(intent);
        });

//        homeBinding.btnWithdraw.setOnClickListener(v -> {
//            Intent intent = new Intent(this, DepositWithdrawActivity.class);
//            intent.putExtra("tab", 1); // 1 = Rút
//            startActivity(intent);
//        });

        homeBinding.navMap.setOnClickListener(v -> {
            startActivity(new Intent(this, MapsActivity.class));

        });

        homeBinding.btnNotify.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });

        homeBinding.btnSave.setOnClickListener(v -> {
            startActivity(new Intent(this, SavingAccountActivity.class));
        });

        homeBinding.btnBill.setOnClickListener(v -> {
            startActivity(new Intent(this, BillPaymentActivity.class));
        });
    }

    /**
     * Setup notification badge
     */
    private void setupNotificationBadge() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        NotificationViewModelFactory factory = new NotificationViewModelFactory(getApplication());
        NotificationViewModel viewModel = new ViewModelProvider(this, factory)
                .get(NotificationViewModel.class);

        viewModel.getUnreadCount(currentUser.getUid()).observe(this, count -> {
            if (count != null && count > 0) {
                homeBinding.tvBadgeCount.setText(String.valueOf(count));
                homeBinding.tvBadgeCount.setVisibility(View.VISIBLE);
            } else {
                homeBinding.tvBadgeCount.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Yêu cầu quyền hiển thị notification cho Android 13+ (API 33+)
     */
    private void requestNotificationPermission() {
        // Chỉ cần yêu cầu cho Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Yêu cầu quyền
                ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
        // Với Android 12 trở xuống, không cần request permission
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                android.util.Log.d("HomeActivity", "Notification permission GRANTED");
            } else {
                // Permission denied
                android.util.Log.w("HomeActivity", "Notification permission DENIED");
                android.widget.Toast.makeText(this,
                    "Bạn sẽ không nhận được thông báo từ app",
                    android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }
}
