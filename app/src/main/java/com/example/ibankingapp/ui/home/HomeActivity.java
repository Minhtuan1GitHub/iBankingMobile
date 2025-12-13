package com.example.ibankingapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.databinding.ActivityHomeBinding;
import com.example.ibankingapp.ui.account.saving.SavingAccountActivity;
import com.example.ibankingapp.ui.login.RegisterActivity;
import com.example.ibankingapp.ui.maps.MapsActivity;
import com.example.ibankingapp.ui.notification.NotificationActivity;
import com.example.ibankingapp.ui.setting.SettingActivity;
import com.example.ibankingapp.ui.transfer.TransferActivity;
import com.example.ibankingapp.ui.transfer.transaction.HistoryTransactionActivity;
import com.example.ibankingapp.viewModel.notification.NotificationViewModel;
import com.example.ibankingapp.viewModel.notification.NotificationViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;


public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding homeBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        homeBinding.fabTransfers .setOnClickListener(v->{
            startActivity(new Intent(this, TransferActivity.class));
        });

        homeBinding.navProfile.setOnClickListener(v->{
            startActivity(new Intent(this, SettingActivity.class));

        });
        homeBinding.navHistory.setOnClickListener(v->{
            startActivity(new Intent(this, HistoryTransactionActivity.class));
        });

        homeBinding.navMap.setOnClickListener(v->{
            startActivity(new Intent(this, MapsActivity.class));

        });
        homeBinding.btnNotify.setOnClickListener(v->{
            startActivity(new Intent(this, NotificationActivity.class));
        });

        NotificationViewModelFactory factory = new NotificationViewModelFactory(getApplication());
        NotificationViewModel viewModel = new ViewModelProvider(this, factory)
                .get(NotificationViewModel.class);

        viewModel.getUnreadCount(FirebaseAuth.getInstance().getCurrentUser().getUid()).observe(this, count->{
            if (count != null && count >0){
                homeBinding.tvBadgeCount.setText(String.valueOf(count));
                homeBinding.tvBadgeCount.setVisibility(View.VISIBLE);
            }else{
                homeBinding.tvBadgeCount.setVisibility(View.GONE);
            }
        });

        homeBinding.btnSave.setOnClickListener(v->{
            startActivity(new Intent(this, SavingAccountActivity.class));
        });
    }
}
