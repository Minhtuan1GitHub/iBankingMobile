package com.example.ibankingapp.ui.transfer.transaction;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.databinding.ActivityBillPaymentBinding;
import com.example.ibankingapp.entity.BillEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.ui.transfer.SuccessfullTransferActivity;
import com.example.ibankingapp.viewModel.transaction.BillViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class BillPaymentActivity extends AppCompatActivity {
    private ActivityBillPaymentBinding binding;
    private BillViewModel viewModel;
    private Customer currentCustomer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBillPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        viewModel = new ViewModelProvider(this).get(BillViewModel.class);
        loadCurrentCustomer();
        binding.btnLookup.setOnClickListener(v->lookUpBill());
        binding.btnPay.setOnClickListener(v->payBill());

    }

    private void lookUpBill(){
        String code = binding.edtCustomerCode.getText().toString();

        if (code.isEmpty()){
            Toast.makeText(this, "Vui lòng nhập mã khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.findBill(code).observe(this, bill ->{
            if (bill == null){
                Toast.makeText(this, "Hóa đơn không tồn tại1", Toast.LENGTH_SHORT).show();
                binding.cardBillInfo.setVisibility(View.GONE);
            }else{
                binding.cardBillInfo.setVisibility(View.VISIBLE);
                binding.tvCustomerCode.setText("Mã khách hàng: " + bill.getCustomerCode());
                binding.tvName.setText("Tên khách hàng: " + bill.getCustomerName());
                binding.tvProvider.setText("Nhà cung cấp: " + bill.getProvider());
                binding.tvPeriod.setText("Hạn thanh toán: " + bill.getPeriod());
                binding.tvStatus.setText("Trạng thái: " + bill.getStatus());
                binding.tvAmount.setText(String.valueOf(bill.getAmount()));
            }
        });
    }
    private void loadCurrentCustomer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentCustomer = doc.toObject(Customer.class);
                        binding.tvBalance.setText("Số dư: " + String.valueOf(getCurrentCustomerBalance()));
                        binding.tvCustomer.setText("Tên khách hàng: " + currentCustomer.getFullName());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi load tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void payBill() {
        String code = binding.edtCustomerCode.getText().toString();

        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.findBill(code).observe(this, bill -> {
            if (bill == null) {
                Toast.makeText(this, "Hóa đơn không tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }
            if(bill.getAmount() > getCurrentCustomerBalance()){
                binding.btnPay.setEnabled(true);
                binding.btnPay.setText("TÀI KHOẢN KHÔNG ĐỦ SỐ DƯ");
                return;
            }
            if (bill.getStatus().equals("Đã thanh toán")){
                binding.btnPay.setEnabled(true);
                binding.btnPay.setText("HÓA ĐƠN ĐÃ THANH TOÁN");
                return;
            }

            showPinDialog(bill);

        });
    }


    private double getCurrentCustomerBalance() {
        return currentCustomer != null ? currentCustomer.getBalance() : 0.0;
    }

    private String getCurrentCustomerAccountNumber() {
        return currentCustomer != null ? currentCustomer.getAccountNumber() : "";
    }

    private void showPinDialog(BillEntity bill) {
        EditText edtPin = new EditText(this);
        edtPin.setHint("Nhập mã PIN");
        edtPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Nhập mã PIN")
                .setView(edtPin)
                .setPositiveButton("OK", (dialog, which) -> {
                    String pin = edtPin.getText().toString();

                    if (pin.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập mã PIN", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!pin.equals(currentCustomer.getOtp())) {
                        Toast.makeText(this, "Mã PIN không đúng", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    doPayPill(bill);

                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void doPayPill(BillEntity bill) {
        double balance = getCurrentCustomerBalance();
        String fromAccount = getCurrentCustomerAccountNumber();

        viewModel.payBill(bill, fromAccount, balance).observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                truTien(bill.getAmount());
                sendNotification(
                        "Thanh toán hóa đơn thành công",
                        "Bạn đã thanh toán hóa đơn " + bill.getCustomerName() + " với số tiền " + bill.getAmount() + "đ từ tài khoản"

                );
                Intent intent = new Intent(this, SuccessfullTransferActivity.class);
                intent.putExtra("from", fromAccount);
                intent.putExtra("to", "");
                intent.putExtra("amount", Double.valueOf(bill.getAmount()));
                intent.putExtra("time", System.currentTimeMillis());
                intent.putExtra("name", bill.getCustomerName());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String title, String message){
        String chanelId = "transfer_chanel";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    chanelId, "Chuyển tiền",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, chanelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }

    private void truTien(long amount){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(
                    db.collection("customers").document(uid));
            double newBalance = snapshot.getDouble("balance") - amount;
            transaction.update(
                    db.collection("customers").document(uid),
                    "balance", newBalance);

            return newBalance;
        });
    }






}
