package com.example.ibankingapp.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.R;
import com.example.ibankingapp.databinding.ActivityHomeBinding;
import com.example.ibankingapp.ui.account.saving.SavingAccountActivity;
import com.example.ibankingapp.ui.login.RegisterActivity;
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


public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding homeBinding;
    private CustomerViewModel customerViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(homeBinding.getRoot());

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        customerViewModel.getImage(uid).observe(this, image->{
            if (image!=null){
                homeBinding.imgAvatar.setImageURI(Uri.parse(image));
            }else{
                homeBinding.imgAvatar.setImageResource(R.drawable.ic_saving);
            }
        });

        homeBinding.fabTransfers .setOnClickListener(v->{
            startActivity(new Intent(this, TransferActivity.class));
        });

        homeBinding.navProfile.setOnClickListener(v->{
            startActivity(new Intent(this, SettingActivity.class));

        });
        homeBinding.navHistory.setOnClickListener(v->{
            startActivity(new Intent(this, HistoryTransactionActivity.class));
        });
        homeBinding.btnDeposit.setOnClickListener(v -> {
            Intent intent = new Intent(this, DepositWithdrawActivity.class);
            intent.putExtra("tab", 0); // 0 = Nạp
            startActivity(intent);
        });

        homeBinding.btnWithdraw.setOnClickListener(v -> {
            Intent intent = new Intent(this, DepositWithdrawActivity.class);
            intent.putExtra("tab", 1); // 1 = Rút
            startActivity(intent);
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

        homeBinding.btnBill.setOnClickListener(v->{
            startActivity(new Intent(this, BillPaymentActivity.class));
        });
    }
}
