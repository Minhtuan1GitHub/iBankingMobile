package com.example.ibankingapp.ui.account.saving;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.data.dao.SavingAccountDao;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivitySavingAccountBinding;
import com.example.ibankingapp.entity.NotificationEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.repository.NotificationRepository;
import com.example.ibankingapp.repository.SavingAccountRepository;

import com.example.ibankingapp.ui.transfer.SuccessfullTransferActivity;
import com.example.ibankingapp.utils.NotificationHelper;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SavingAccountActivity extends AppCompatActivity {

    private ActivitySavingAccountBinding binding;
    private SavingAccountViewModel viewModel;
    private CustomerViewModel customerViewModel;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAW
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavingAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SavingAccountDao dao =
                AppDatabase.getInstance(this).savingAccountDao();

        SavingAccountRepository repo =
                new SavingAccountRepository(dao);

        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @Override
                    public <T extends ViewModel> T create(Class<T> modelClass) {
                        return (T) new SavingAccountViewModel(repo);
                    }
                }
        ).get(SavingAccountViewModel.class);

        customerViewModel = new ViewModelProvider(this)
                .get(CustomerViewModel.class);


        String customerId = FirebaseAuth.getInstance()
                .getCurrentUser()
                        .getUid();
        viewModel.syncFromFirestore(customerId);


        viewModel.getSavingAccounts(customerId)
                .observe(this, account -> {
                    if (account == null) return;


                    binding.tvBalanceValue.setText(account.getBalance() + " VNĐ");
                    binding.tvInterestRateValue.setText(account.getInterestRate() + "% / năm");
                    binding.tvTermValue.setText(account.getTermMonths() + " tháng");

                    String dueDate = DateUtils.formatDateTime(
                            this,
                            account.getDueDate(),
                            DateUtils.FORMAT_SHOW_DATE
                    );
                    binding.tvDueDateValue.setText(dueDate);
                });

        binding.btnDeposit.setOnClickListener(v->showDeposit());
        binding.btnConfirmDeposit.setOnClickListener(v->confirmDeposit());
        binding.btnWithdraw.setOnClickListener(v->showWithdraw());
        binding.btnConfirmWithdraw.setOnClickListener(v->confirmWithdraw());
    }

    //deposit
    private void showDeposit() {
        binding.cardDepositTransaction.setVisibility(View.VISIBLE);
        binding.cardWithdrawTransaction.setVisibility(View.GONE);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        customerViewModel.getCustomer(uid)
                .observe(this, c -> {
                    if (c == null) return;

                    binding.tvDepositSourceAccountNumber
                            .setText("Số tài khoản: " + c.getAccountNumber());
                    binding.tvDepositSourceAccountName
                            .setText("Tên chủ tài khoản: " + c.getFullName());
                    binding.tvDepositSourceBalance
                            .setText("Số dư: " + c.getBalance() + " VNĐ");
                });

    }

    private void confirmDeposit() {
        String amountStr =
                binding.etDepositAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            binding.etDepositAmount.setError("Nhập số tiền");
            return;
        }

        double amount = Double.parseDouble(amountStr);

        showPinDialog(amount, TransactionType.DEPOSIT);
    }
    private void showPinDialog(double amount, TransactionType type){
        EditText input = new EditText(this);
        input.setInputType(
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        new AlertDialog.Builder(this)
                .setTitle("Nhập mã PIN")
                .setMessage("Nhập mã PIN để xác nhận giao dịch")
                .setView(input)
                .setPositiveButton("Xác nhận",(d,w)->{
                    String pin = input.getText().toString();
                    verifyPin(pin, amount, type);
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
    private void verifyPin(String pin, double amount, TransactionType type) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        customerViewModel.verifyPin(uid, pin)
                .observe(this, success -> {
                    if (Boolean.TRUE.equals(success)){

                        if (type == TransactionType.WITHDRAW){
                            customerViewModel.withdraw(uid, amount);
                        }else{
                            customerViewModel.deposit(uid, amount);
                        }


                        customerViewModel.getCustomer(uid)
                                .observe(this, c->{
                                    Intent i = new Intent(this, SuccessfullTransferActivity.class);
                                    i.putExtra("from", c.getAccountNumber());
                                    i.putExtra("to", "Tài khoản tiết kiệm");
                                    i.putExtra("amount", amount);
                                    i.putExtra("time", System.currentTimeMillis());
                                    i.putExtra("name", c.getFullName());
                                    startActivity(i);
                                });
                        NotificationEntity notification = new NotificationEntity();
                        notification.setCustomerId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        notification.setTitle("Chuyển tiền thành công");
                        notification.setMessage("Bạn đã chuyển " + amount + "đ đến số tài khoản " + "Tài khoản tiết kiệm");
                        notification.setTimestamp(System.currentTimeMillis());
                        notification.setRead(false); // chưa đọc

                        // Lưu vào Room
                        NotificationRepository repo = new NotificationRepository(AppDatabase.getInstance(this).notificationDao());
                        repo.addNotification(notification);

                        // Hiển thị Notification Android

                        NotificationHelper.send(
                                this,
                                notification.getTitle(),
                                notification.getMessage()
                        );

                    }else{
                        Toast.makeText(this, "Mã PIN không đúng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // withdraw
    private void showWithdraw(){
        binding.cardWithdrawTransaction.setVisibility(View.VISIBLE);
        binding.cardDepositTransaction.setVisibility(View.GONE);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        customerViewModel.getCustomer(uid)
                .observe(this, c -> {
                    if (c == null) return;

                    binding.tvWithdrawDestAccountNumber
                            .setText("Số tài khoản: " + c.getAccountNumber());
                    binding.tvWithdrawDestAccountName
                            .setText("Tên chủ tài khoản: " + c.getFullName());
                    binding.tvWithdrawDestBalance
                            .setText("Số dư: " + c.getBalance() + " VNĐ");
                });
    }

    private void confirmWithdraw() {
        String amountStr = binding.etWithdrawAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            binding.etWithdrawAmount.setError("Nhập số tiền");
            return;
        }

        double amount = Double.parseDouble(amountStr);

        showPinDialog(amount, TransactionType.WITHDRAW);
    }
}

