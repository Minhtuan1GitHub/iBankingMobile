package com.example.ibankingapp.ui.account.saving;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivitySavingAccountBinding;
import com.example.ibankingapp.entity.NotificationEntity;
import com.example.ibankingapp.entity.SavingAccountEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.NotificationRepository;
import com.example.ibankingapp.repository.SavingAccountRepository;
import com.example.ibankingapp.utils.NotificationHelper;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModel;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SavingAccountActivity extends AppCompatActivity {

    private ActivitySavingAccountBinding binding;
    private SavingAccountViewModel savingAccountViewModel;
    private CustomerViewModel customerViewModel;
    private NotificationRepository notificationRepository;

    private SavingAccountEntity currentAccount;
    private Customer currentCustomerModel;

    private enum TransactionType {
        DEPOSIT, WITHDRAW
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeStatusBarTransparent();

        binding = ActivitySavingAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        initViewModels();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadSavingAccountData(uid);
        loadCustomerBalance(uid);
        setupClickEvents();
    }

    private void makeStatusBarTransparent() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void initViewModels() {
        SavingAccountRepository repo = new SavingAccountRepository(AppDatabase.getInstance(this).savingAccountDao());
        SavingAccountViewModelFactory factory = new SavingAccountViewModelFactory(repo);
        savingAccountViewModel = new ViewModelProvider(this, factory).get(SavingAccountViewModel.class);

        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);
        notificationRepository = new NotificationRepository(AppDatabase.getInstance(this).notificationDao());
    }

    private void loadSavingAccountData(String uid) {
        savingAccountViewModel.syncFromFirestore(uid);
        savingAccountViewModel.getSavingAccounts(uid).observe(this, account -> {
            if (account != null) {
                currentAccount = account;
                updateUI(account);
            } else {
                binding.tvAccountName.setText("CHƯA CÓ TÀI KHOẢN");
                binding.tvBalanceValue.setText("0 VNĐ");
                binding.tvAccountNumberValue.setText("---");
            }
        });
    }

    private void updateUI(SavingAccountEntity account) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        binding.tvBalanceValue.setText(currencyFormat.format(account.getBalance()));
        binding.tvAccountNumberValue.setText(account.getAccountNumber());
        binding.tvInterestRateValue.setText(account.getInterestRate() + "% / năm");
        binding.tvTermValue.setText(account.getTermMonths() + " tháng");

        if (account.getDueDate() > 0) {
            binding.tvDueDateValue.setText(dateFormat.format(new Date(account.getDueDate())));
        } else {
            binding.tvDueDateValue.setText("---");
        }
    }

    private void loadCustomerBalance(String uid) {
        customerViewModel.getCustomer(uid).observe(this, customer -> {
            if (customer != null) {
                currentCustomerModel = customer; // Lưu lại để check số dư khi giao dịch

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                String balanceStr = currencyFormat.format(customer.getBalance());

                binding.tvDepositSourceAccountName.setText(customer.getFullName());
                binding.tvDepositSourceAccountNumber.setText(customer.getAccountNumber());
                binding.tvDepositSourceBalance.setText("Số dư khả dụng: " + balanceStr);

                binding.tvWithdrawDestAccountName.setText(customer.getFullName());
                binding.tvWithdrawDestAccountNumber.setText(customer.getAccountNumber());
                binding.tvWithdrawDestBalance.setText("Số dư TK chính: " + balanceStr);
            }
        });
    }

    private void setupClickEvents() {
        binding.btnDeposit.setOnClickListener(v -> {
            binding.cardDepositTransaction.setVisibility(View.VISIBLE);
            binding.cardWithdrawTransaction.setVisibility(View.GONE);
            binding.etDepositAmount.requestFocus();
        });

        binding.btnWithdraw.setOnClickListener(v -> {
            binding.cardWithdrawTransaction.setVisibility(View.VISIBLE);
            binding.cardDepositTransaction.setVisibility(View.GONE);
            binding.etWithdrawAmount.requestFocus();
        });

        binding.btnConfirmDeposit.setOnClickListener(v -> handleDeposit());
        binding.btnConfirmWithdraw.setOnClickListener(v -> handleWithdraw());
    }

    private void handleDeposit() {
        if (currentAccount == null) {
            Toast.makeText(this, "Bạn chưa có tài khoản tiết kiệm", Toast.LENGTH_SHORT).show();
            return;
        }
        String amountStr = binding.etDepositAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            binding.etDepositAmount.setError("Vui lòng nhập số tiền");
            return;
        }
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 100000) {
                binding.etDepositAmount.setError("Tối thiểu 100.000 VNĐ");
                return;
            }
            if (currentCustomerModel != null && amount > currentCustomerModel.getBalance()) {
                binding.etDepositAmount.setError("Số dư ví không đủ");
                return;
            }
            showPinDialog(amount, TransactionType.DEPOSIT);
        } catch (NumberFormatException e) {
            binding.etDepositAmount.setError("Số tiền không hợp lệ");
        }
    }

    private void handleWithdraw() {
        if (currentAccount == null) return;
        String amountStr = binding.etWithdrawAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            binding.etWithdrawAmount.setError("Vui lòng nhập số tiền");
            return;
        }
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount > currentAccount.getBalance()) {
                binding.etWithdrawAmount.setError("Số dư tiết kiệm không đủ");
                return;
            }
            showPinDialog(amount, TransactionType.WITHDRAW);
        } catch (NumberFormatException e) {
            binding.etWithdrawAmount.setError("Số tiền không hợp lệ");
        }
    }

    private void showPinDialog(double amount, TransactionType type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác thực giao dịch");
        builder.setMessage("Nhập mã PIN để xác nhận.");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String pin = input.getText().toString();
            if ("123456".equals(pin)) {
                if (type == TransactionType.DEPOSIT) {
                    processDeposit(amount);
                } else {
                    processWithdraw(amount);
                }
            } else {
                Toast.makeText(this, "Mã PIN không đúng", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // --- XỬ LÝ GIAO DỊCH THẬT (BACKEND) ---

    private void processDeposit(double amount) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. Trừ tiền Ví chính (Wallet)
        // Sử dụng hàm walletWithdraw có sẵn trong CustomerViewModel để đảm bảo logic
        customerViewModel.walletWithdraw(uid, amount).observe(this, result -> {
            if (result.isSuccess()) {
                // 2. Nếu trừ ví thành công -> Cộng tiền vào TK Tiết kiệm
                double newSavingBalance = currentAccount.getBalance() + amount;
                currentAccount.setBalance(newSavingBalance);
                savingAccountViewModel.updateSavingAccount(currentAccount);

                // 3. Thông báo thành công
                String msg = "Đã nạp " + String.format("%,.0f", amount) + " VNĐ vào tiết kiệm.";
                createNotification("Nạp tiết kiệm thành công", msg);
                Toast.makeText(this, "Giao dịch thành công!", Toast.LENGTH_SHORT).show();

                // Reset giao diện
                binding.cardDepositTransaction.setVisibility(View.GONE);
                binding.etDepositAmount.setText("");
            } else {
                Toast.makeText(this, "Lỗi trừ tiền ví: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processWithdraw(double amount) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 1. Trừ tiền TK Tiết kiệm trước
        double newSavingBalance = currentAccount.getBalance() - amount;
        currentAccount.setBalance(newSavingBalance);
        savingAccountViewModel.updateSavingAccount(currentAccount);

        // 2. Cộng tiền vào Ví chính (Wallet)
        // Sử dụng hàm walletDeposit có sẵn
        customerViewModel.walletDeposit(uid, amount).observe(this, result -> {
            if (result.isSuccess()) {
                String msg = "Đã rút " + String.format("%,.0f", amount);
                createNotification("Rút tiết kiệm thành công", msg);
                Toast.makeText(this, "Giao dịch thành công!", Toast.LENGTH_SHORT).show();

                binding.cardWithdrawTransaction.setVisibility(View.GONE);
                binding.etWithdrawAmount.setText("");
            } else {
                Toast.makeText(this, "Lỗi cộng tiền ví: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotification(String title, String message) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        NotificationEntity notification = new NotificationEntity();
        notification.setCustomerId(uid);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTimestamp(System.currentTimeMillis());
        notification.setRead(false);

        new Thread(() -> notificationRepository.addNotification(notification)).start();
        NotificationHelper.send(this, title, message);
    }
}