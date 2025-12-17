package com.example.ibankingapp.ui.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.R;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivitySettingBinding;
import com.example.ibankingapp.ui.keyc.EkycActivity;
import com.example.ibankingapp.ui.login.LoginActivity;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executors;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding settingBinding;
    private CustomerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingBinding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(settingBinding.getRoot());
        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.getImage(uid).observe(this, image->{
            if (image!=null){
                settingBinding.ivAvatar.setImageURI(Uri.parse(image));
            }else{
                settingBinding.ivAvatar.setImageResource(R.drawable.ic_saving);
            }
        });

        settingBinding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            AppDatabase db = AppDatabase.getInstance(this);
            Executors.newSingleThreadExecutor().execute(() -> {
                db.customerDao().clearAll();
            });

            startActivity(new Intent(this, LoginActivity.class));
            finish();

        });

        settingBinding.btnEkycSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, EkycActivity.class));
        });
    }

}
