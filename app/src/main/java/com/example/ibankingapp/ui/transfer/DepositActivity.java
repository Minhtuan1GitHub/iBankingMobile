package com.example.ibankingapp.ui.transfer;

import android.os.Bundle;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.repository.CustomerRepository;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;

public class DepositActivity extends AppCompatActivity {
//    private ActivityBinding binding;
//    private CustomerRepository repository;
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityDepositBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//
//        // ZaloPay SDK Init
//        ZaloPaySDK.init(553, Environment.SANDBOX);
//        // bind components with ids
//        BindView();
//        // handle CreateOrder
//        btnCreateOrder.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onClick(View v) {
//                CreateOrder orderApi = new CreateOrder();
//
//                try {
//                    JSONObject data = orderApi.createOrder(txtAmount.getText().toString());
//                    Log.d("Amount", txtAmount.getText().toString());
//                    lblZpTransToken.setVisibility(View.VISIBLE);
//                    String code = data.getString("returncode");
//                    Toast.makeText(getApplicationContext(), "return_code: " + code, Toast.LENGTH_LONG).show();
//
//                    if (code.equals("1")) {
//                        lblZpTransToken.setText("zptranstoken");
//                        txtToken.setText(data.getString("zptranstoken"));
//                        IsDone();
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
}
