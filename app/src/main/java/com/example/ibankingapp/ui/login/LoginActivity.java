package com.example.ibankingapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.databinding.ActivityLoginBinding;
import com.example.ibankingapp.ui.admin.AdminActivity;
import com.example.ibankingapp.ui.home.HomeActivity;
import com.example.ibankingapp.utils.BankSeeder;
import com.example.ibankingapp.utils.BillSeeder;
import com.example.ibankingapp.utils.InterbankAccountSeeder;
import com.example.ibankingapp.utils.PhoneSeeder;
import com.example.ibankingapp.utils.TelcoSeeder;
import com.example.ibankingapp.utils.TopupPackageSeeder;
import com.example.ibankingapp.viewModel.login.LoginViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding loginBinding;
    private LoginViewModel viewModelLogin;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BillSeeder.seedBill();
        TelcoSeeder.seedTelcoProvider();
        TopupPackageSeeder.seedPackagesOnce();
        PhoneSeeder.seedPhone();
        BankSeeder.seedBanks();
        InterbankAccountSeeder.seedAccounts();
        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());
        viewModelLogin = new ViewModelProvider(this).get(LoginViewModel.class);

        loginBinding.setVm(viewModelLogin);
        loginBinding.setLifecycleOwner(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        observeData();
    }

    private void observeData() {
        viewModelLogin.getLoginResult().observe(this, success -> {
            if (success) {
                checkRoleAndNavigate();
            } else {
                Toast.makeText(this, "Đăng nhập thất bại. Kiểm tra lại email/mật khẩu.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkRoleAndNavigate() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("customers").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        Intent intent = null;
                        if ("customer".equals(role)) {
                            intent = new Intent(this, HomeActivity.class);
                        } else if ("employment".equals(role)) { // Đã khớp với DB của bạn
                            intent = new Intent(this, AdminActivity.class);
                        } else if ("admin".equals(role)) { // Phòng hờ nếu bạn có dùng role "admin"
                            intent = new Intent(this, AdminActivity.class);
                        }

                        if (intent != null) {

                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Role lạ -> Không cho vào
                            Toast.makeText(this, "Quyền truy cập không hợp lệ: " + role, Toast.LENGTH_LONG).show();
                            auth.signOut(); // Đăng xuất để tránh kẹt
                        }

                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin tài khoản trên hệ thống.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kết nối Server: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}