package com.example.ibankingapp.ui.account.saving;

import android.os.Bundle;
import android.text.format.DateUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.data.dao.SavingAccountDao;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivitySavingAccountBinding;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.SavingAccountRepository;

import com.example.ibankingapp.viewModel.customer.SavingAccountViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class SavingAccountActivity extends AppCompatActivity {

    private ActivitySavingAccountBinding binding;
    private SavingAccountViewModel viewModel;
    private Customer customer;

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
    }
}

