package com.example.ibankingapp.ui.transfer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ibankingapp.Api.CreateOrder;
import com.example.ibankingapp.databinding.ActivityTransactionIntentBinding;

import org.json.JSONObject;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class TransactionIntentActivity extends AppCompatActivity {
    private ActivityTransactionIntentBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransactionIntentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(553, Environment.SANDBOX);

        Intent intent = getIntent();

        binding.tvAmountLabel.setText(intent.getStringExtra("amount") + " VND");
        binding.tvRecipientNameValue.setText(intent.getStringExtra("recipientName"));
        binding.tvRecipientAccountValue.setText(intent.getStringExtra("recipientAccount"));

        // handle CreateOrder
        binding.btnPay.setOnClickListener(v -> {
            CreateOrder orderApi = new CreateOrder();

            try {
                JSONObject data = orderApi.createOrder(binding.tvAmountValue.getText().toString());
                Log.d("Amount", binding.tvAmountValue.getText().toString());
                String code = data.getString("returncode");

                if (code.equals("1")) {
                    String token = data.getString("zptranstoken");
                    ZaloPaySDK.getInstance().payOrder(TransactionIntentActivity.this, token, "demozpdk://app", new PayOrderListener() {
                        @Override
                        public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                            Log.d("ZaloPay", "Payment succeeded. Transaction ID: " + transactionId);
                            // Handle success (e.g., show a success message, update UI, etc.)
                        }

                        @Override
                        public void onPaymentCanceled(String zpTransToken, String appTransID) {
                            Log.d("ZaloPay", "Payment canceled. App Transaction ID: " + appTransID);
                            // Handle cancellation (e.g., show a cancellation message, update UI, etc.)
                        }

                        @Override
                        public void onPaymentError(vn.zalopay.sdk.ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                            Log.d("ZaloPay", "Payment error. Error: " + zaloPayError.toString() + ", zpTransToken: " + zpTransToken + ", App Transaction ID: " + appTransID);
                            // Handle error (e.g., show an error message, update UI, etc.)
                        }
                    });
                } else {
                    Log.e("CreateOrder", "Failed to create order. Return code: " + code);
                }

            } catch (Exception e) {
                Log.e("TransactionIntent", "Error creating order", e);
            }
        });
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
}
