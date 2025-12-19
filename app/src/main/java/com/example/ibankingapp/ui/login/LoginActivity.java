package com.example.ibankingapp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.databinding.ActivityLoginBinding;
import com.example.ibankingapp.ui.admin.AdminActivity;
import com.example.ibankingapp.ui.home.HomeActivity;
import com.example.ibankingapp.utils.BillSeeder;
import com.example.ibankingapp.utils.PhoneSeeder;
import com.example.ibankingapp.utils.TelcoSeeder;
import com.example.ibankingapp.utils.TopupPackageSeeder;
import com.example.ibankingapp.viewModel.login.FirebaseAuthManager;
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
        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());
        viewModelLogin = new ViewModelProvider(this).get(LoginViewModel.class);

        loginBinding.setVm(viewModelLogin);
        loginBinding.setLifecycleOwner(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        observeData();
    }

    private void observeData(){
        viewModelLogin.getLoginResult().observe(this, success->{
            if (success){
//                startActivity(new Intent(this, AdminActivity.class));
                // check role
                String uid = auth.getCurrentUser().getUid();
                db.collection("customers").document(uid).get()
                        .addOnSuccessListener(doc->{
                            if (doc.exists()){
                                String role = doc.getString("role");
                                if ("customer".equals(role)){
                                    startActivity(new Intent(this, HomeActivity.class));
                                } else if ("employment".equals(role)){
                                    startActivity(new Intent(this, AdminActivity.class));
                                }
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Không lấy được dữ liệu user", Toast.LENGTH_SHORT).show();
                        });

            }else{
                Toast.makeText(this, "Login faire", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
