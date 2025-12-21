package com.example.ibankingapp.ui.notification;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ibankingapp.databinding.ActivityNotificationBinding;
import com.example.ibankingapp.viewModel.notification.NotificationViewModel;
import com.example.ibankingapp.viewModel.notification.NotificationViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NotificationActivity extends AppCompatActivity {
    private ActivityNotificationBinding binding;
    private NotificationAdapter adapter;
    private NotificationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new NotificationAdapter();

        binding.rvNotifications.setAdapter(adapter);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        NotificationViewModelFactory factory = new NotificationViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(NotificationViewModel.class);

        viewModel.getNotifications(user.getUid()).observe(this, notifications -> {
            adapter.setData(notifications);


            if (notifications == null || notifications.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvNotifications.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvNotifications.setVisibility(View.VISIBLE);
            }
        });

        // Đánh dấu đã đọc
        viewModel.markAsRead(user.getUid());

        binding.btnBack.setOnClickListener(v -> finish());
    }
}