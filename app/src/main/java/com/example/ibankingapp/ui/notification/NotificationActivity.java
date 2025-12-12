package com.example.ibankingapp.ui.notification;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityNotificationBinding;
import com.example.ibankingapp.repository.NotificationRepository;
import com.example.ibankingapp.viewModel.notification.NotificationViewModel;
import com.example.ibankingapp.viewModel.notification.NotificationViewModelFactory;

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
        binding.recyclerNoti.setAdapter(adapter);
        binding.recyclerNoti.setLayoutManager(new LinearLayoutManager(this));

        // Dùng Factory đúng
        NotificationViewModelFactory factory = new NotificationViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(NotificationViewModel.class);

        viewModel.getNotifications().observe(this, notifications -> {
            adapter.setData(notifications);
        });

        // Đánh dấu tất cả notification là đã đọc
        viewModel.markAsRead();
    }


}