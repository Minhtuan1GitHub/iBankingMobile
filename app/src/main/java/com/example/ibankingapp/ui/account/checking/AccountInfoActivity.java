package com.example.ibankingapp.ui.account.checking;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.ibankingapp.databinding.ActivityAccountInfoBinding;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class AccountInfoActivity extends AppCompatActivity {

    private ActivityAccountInfoBinding binding;
    private CustomerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        makeStatusBarTransparent();

        binding = ActivityAccountInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        loadCustomerData();
    }

    private void loadCustomerData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        viewModel.getCustomer(uid).observe(this, customer -> {
            if (customer != null) {

                binding.setCustomer(customer);
            }
        });
    }

    private void makeStatusBarTransparent() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}