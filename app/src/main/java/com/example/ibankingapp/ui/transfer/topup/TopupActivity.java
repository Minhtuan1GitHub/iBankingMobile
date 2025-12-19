package com.example.ibankingapp.ui.transfer.topup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityTopupBinding;
import com.example.ibankingapp.entity.NotificationEntity;
import com.example.ibankingapp.entity.TopupEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.NotificationRepository;
import com.example.ibankingapp.ui.transfer.SuccessfullTransferActivity;
import com.example.ibankingapp.utils.NotificationHelper;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.example.ibankingapp.viewModel.topup.TopupViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TopupActivity extends AppCompatActivity {
    private ActivityTopupBinding binding;
    private CustomerViewModel customerViewModel;
    private TopupViewModel topupViewModel;
    private List<TopupEntity> currentTopups = new ArrayList<>();
    private long walletBalance = 0;
    private TopupEntity selected;
    private Customer currentCustomer;






    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityTopupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        topupViewModel = new ViewModelProvider(this).get(TopupViewModel.class);



        setupProviderSpinner();
        binding.btnSubmitTopUp.setEnabled(false);
        binding.btnSubmitTopUp.setAlpha(0.5f);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        customerViewModel.getCustomer(uid).observe(this, c->{
            if (c == null){
                binding.tvWalletBalance.setText("no");
                walletBalance = 0;
                return;
            }
            currentCustomer = c;
            binding.tvWalletBalance.setText(String.valueOf(c.getBalance()));
            walletBalance = (long) c.getBalance();
        });

        binding.edtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = s.toString().trim();
                if (s.length() >= 3) {
                    String provider = detectProvider(s.toString());

                    if (!provider.isEmpty()) {
                        binding.spinnerProvider.setText(provider, false);
                        loadAmountFromFirebase(provider);
                    }

                    customerViewModel.getPhone(phone).observe(TopupActivity.this, phoneEntity -> {
                        if (phoneEntity != null) {
                            binding.cardTopUpInfo.setVisibility(View.VISIBLE);
                            binding.tvConfirmPhone.setText("Số điện thoại: "+ phoneEntity.getPhone());
                            binding.tvName.setText("Họ tên: "+ phoneEntity.getName());
                            binding.tvConfirmProvider.setText("Nhà mạng: "+ phoneEntity.getProvider());
                        }
                    });
                }
                checkEnableSubmit();
            }
        });



        binding.spinnerAmount.setFocusable(false);
        binding.spinnerAmount.setClickable(true);
        binding.spinnerAmount.setOnClickListener(v ->
                binding.spinnerAmount.showDropDown()
        );

        binding.btnSubmitTopUp.setOnClickListener(v -> pay());
    }

    private void pay(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        showPin(uid);
    }
    private void showPin(String uid) {
        EditText edtPin = new EditText(this);
        edtPin.setHint("Nhập mã PIN");
        edtPin.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Nhập mã PIN")
                .setView(edtPin)
                .setPositiveButton("OK", null) // ⚠️ để null
                .setNegativeButton("Huỷ", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String pin = edtPin.getText().toString().trim();

                        customerViewModel.verifyPin(uid, pin).observe(this, ok -> {
                            if (ok) {
                                dialog.dismiss();
                                doPay();
                            } else {
                                Toast.makeText(this, "Sai mã PIN", Toast.LENGTH_SHORT).show();
                            }
                        });



                    });
        });

        dialog.show();
    }

    private void doPay(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String phone = binding.edtPhoneNumber.getText().toString().trim();
        long amount = selected.getAmount();

        topupViewModel.topup(uid, phone, amount).observe(this, success->{
            if (success != null && success){
                NotificationEntity notification = new NotificationEntity();
                notification.setCustomerId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                notification.setTitle("Nạp tiền thành công");
                notification.setMessage("Đã nạp tiền thành công đến số tài khoản " + phone + " với số tiền " + amount + "đ");
                notification.setTimestamp(System.currentTimeMillis());
                notification.setRead(false); // chưa đọc

                // Lưu vào Room
                NotificationRepository repo = new NotificationRepository(AppDatabase.getInstance(this).notificationDao());
                repo.addNotification(notification);

                // Hiển thị Notification Android
                NotificationHelper.send(
                        this,
                        notification.getTitle(),
                        notification.getMessage());

                Toast.makeText(this, "Nạp tiền thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, SuccessfullTransferActivity.class);
                intent.putExtra("from", currentCustomer.getAccountNumber());
                intent.putExtra("to", phone);
                intent.putExtra("amount", amount);
                intent.putExtra("time", System.currentTimeMillis());
                intent.putExtra("name", currentCustomer.getFullName());
                startActivity(intent);
            }else{
                Toast.makeText(this, "Nạp tiền thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String detectProvider(String phone) {
        if (phone.startsWith("096") || phone.startsWith("097") || phone.startsWith("098")
                || phone.startsWith("086") || phone.startsWith("032") || phone.startsWith("033")
                || phone.startsWith("034") || phone.startsWith("035")
                || phone.startsWith("036") || phone.startsWith("037")
                || phone.startsWith("038") || phone.startsWith("039")) {
            return "Viettel";
        }

        if (phone.startsWith("090") || phone.startsWith("093")
                || phone.startsWith("070") || phone.startsWith("076")
                || phone.startsWith("077") || phone.startsWith("078")
                || phone.startsWith("079")) {
            return "Mobifone";
        }

        if (phone.startsWith("091") || phone.startsWith("094")
                || phone.startsWith("081") || phone.startsWith("082")
                || phone.startsWith("083") || phone.startsWith("084")
                || phone.startsWith("085")) {
            return "Vinaphone";
        }

        return "";
    }



    private void checkEnableSubmit() {
        String phone = binding.edtPhoneNumber.getText().toString().trim();
        String provider = binding.spinnerProvider.getText().toString().trim();
        String amountText = binding.spinnerAmount.getText().toString().trim();

        boolean basicValid = phone.length() == 10
                && !provider.isEmpty()
                && !amountText.isEmpty()
                && selected != null;

        boolean enoughMoney = false;

        if (selected != null) {
            enoughMoney = walletBalance >= selected.getPrice();
        }

        boolean enable = basicValid && enoughMoney;

        binding.btnSubmitTopUp.setEnabled(enable);
        binding.btnSubmitTopUp.setAlpha(enable ? 1f : 0.5f);

    }

    private void setupProviderSpinner() {
        List<String> providers = Arrays.asList(
                "Viettel",
                "Mobifone",
                "Vinaphone"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                providers
        );

        binding.spinnerProvider.setAdapter(adapter);

        binding.spinnerProvider.setOnItemClickListener((parent, view, position, id) -> {
            String provider = parent.getItemAtPosition(position).toString();

            loadAmountFromFirebase(provider);
            checkEnableSubmit();
        });


    }
    private void loadAmountFromFirebase(String provider) {

        binding.spinnerAmount.setEnabled(false);

        topupViewModel.getTopups(provider).removeObservers(this);

        topupViewModel.getTopups(provider)
                .observe(this, list -> {
                    Log.d("TOPUP", "SIZE = " + (list == null ? 0 : list.size()));

                    if (list == null || list.isEmpty()) return;

                    currentTopups.clear();
                    currentTopups.addAll(list);

                    List<String> display = new ArrayList<>();
                    for (TopupEntity t : list) {
                        display.add(String.format("%,dđ", t.getAmount()));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_list_item_1,
                            display
                    );

                    binding.spinnerAmount.setAdapter(adapter);
                    binding.spinnerAmount.setText("", false);
                    binding.spinnerAmount.setEnabled(true);
                });
        binding.spinnerAmount.setOnItemClickListener((parent, view, position, id) -> {

            selected = currentTopups.get(position);

            long amount = selected.getAmount();
            long price = selected.getPrice();
            long discount = selected.getDiscount();

            // Nếu muốn hiển thị:
            // binding.tvAmount.setText(String.format("%,dđ", amount));

            checkEnableSubmit();
        });

    }






}
