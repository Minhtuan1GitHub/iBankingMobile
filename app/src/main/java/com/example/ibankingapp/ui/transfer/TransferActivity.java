package com.example.ibankingapp.ui.transfer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.databinding.ActivityTransferBinding;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.ui.home.HomeActivity;

public class TransferActivity extends AppCompatActivity {
    private ActivityTransferBinding transferBinding;
    private Customer currentCustomer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transferBinding = ActivityTransferBinding.inflate(getLayoutInflater());
        setContentView(transferBinding.getRoot());


        transferBinding.fabHome.setOnClickListener(v->{
            startActivity(new Intent(this, HomeActivity.class));
        });

        //transferBinding.tvSourceBalance.setText(String.valueOf(currentCustomer.getBalance()));

    }
}
