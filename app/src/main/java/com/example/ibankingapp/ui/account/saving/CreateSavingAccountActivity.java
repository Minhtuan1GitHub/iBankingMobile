package com.example.ibankingapp.ui.account.saving;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityCreateSavingAccountBinding;
import com.example.ibankingapp.entity.SavingAccountEntity;
import com.example.ibankingapp.repository.SavingAccountRepository;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModel;

public class CreateSavingAccountActivity extends AppCompatActivity {
    private ActivityCreateSavingAccountBinding binding;
    private SavingAccountViewModel viewModel;
    private String customerId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateSavingAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        customerId = getIntent().getStringExtra("customerId");

        AppDatabase db = Room.databaseBuilder(
                this,
                AppDatabase.class,
                "customer_db"
        ).allowMainThreadQueries().build();

        SavingAccountRepository repo = new SavingAccountRepository(db.savingAccountDao());
        viewModel = new SavingAccountViewModel(repo);

        binding.edtAccountNumber.setText(customerId);

        binding.btnCreateSaving.setOnClickListener(v -> createSavingAccout());
    }

    private void createSavingAccout() {
        double amount = Double.parseDouble(binding.edtAmount.getText().toString());
        String termString = binding.spnTerm.getSelectedItem().toString(); // "3 tháng"


        String term = termString.split(" ")[0]; // → "3"

        Long termLong = Long.parseLong(term);

        double interestRate = Double.parseDouble(binding.edtInterestRate.getText().toString());

        String accountNumber = "STK" + ((int)(Math.random() * 900000) + 100000);

        SavingAccountEntity account = new SavingAccountEntity(
                "",
                customerId,
                accountNumber,
                amount,
                interestRate,
                termLong,
                System.currentTimeMillis(),
                System.currentTimeMillis()


        );

        viewModel.createSavingAccount(account);

        finish();



    }


}
