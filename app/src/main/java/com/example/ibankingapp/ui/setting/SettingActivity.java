package com.example.ibankingapp.ui.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.R;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivitySettingBinding;
import com.example.ibankingapp.ui.account.checking.AccountInfoActivity;
import com.example.ibankingapp.ui.keyc.EkycActivity;
import com.example.ibankingapp.ui.login.LoginActivity;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executors;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding settingBinding;
    private CustomerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingBinding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(settingBinding.getRoot());

        settingBinding.toolbar.setNavigationOnClickListener(v -> finish());


        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUserData(uid);

        setupClickEvents();
    }

    private void loadUserData(String uid) {

        viewModel.getCustomer(uid).observe(this, customer -> {
            if (customer != null) {

                settingBinding.setUserName(customer.getFullName());
                settingBinding.setAccountNumber("STK: " + customer.getAccountNumber());
            }
        });


        viewModel.getImage(uid).observe(this, image -> {
            if (image != null) {
                settingBinding.ivAvatar.setImageURI(Uri.parse(image));
            } else {
                settingBinding.ivAvatar.setImageResource(R.drawable.ic_account_circle_24); // Đổi icon mặc định cho hợp lý
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu");

        // Tạo Layout chứa 2 ô nhập liệu
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText edtNewPass = new EditText(this);
        edtNewPass.setHint("Mật khẩu mới (tối thiểu 6 ký tự)");
        edtNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(edtNewPass);

        final EditText edtConfirmPass = new EditText(this);
        edtConfirmPass.setHint("Nhập lại mật khẩu mới");
        edtConfirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(edtConfirmPass);

        builder.setView(layout);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String newPass = edtNewPass.getText().toString().trim();
            String confirmPass = edtConfirmPass.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(newPass);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void changePassword(String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void setupClickEvents() {
        settingBinding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        // Nút Đăng xuất
        settingBinding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            AppDatabase db = AppDatabase.getInstance(this);
            Executors.newSingleThreadExecutor().execute(() -> {
                db.customerDao().clearAll();
            });
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa stack để không back lại được
            startActivity(intent);
            finish();
        });

        // Nút Cài đặt eKYC
        settingBinding.btnEkycSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, EkycActivity.class));
        });

        // Nút Tra cứu
        settingBinding.btnLookup.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountInfoActivity.class));
        });
    }
}