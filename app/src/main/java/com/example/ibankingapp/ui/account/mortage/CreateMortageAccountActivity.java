package com.example.ibankingapp.ui.account.mortage;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityCreatingMortageAccountBinding;
import com.example.ibankingapp.entity.MortageEntity;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.repository.MortageRepository;
import com.example.ibankingapp.repository.SavingAccountRepository;
import com.example.ibankingapp.repository.TransactionRepository;
import com.example.ibankingapp.viewModel.customer.MortageViewModel;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModel;

public class CreateMortageAccountActivity extends AppCompatActivity {
    private ActivityCreatingMortageAccountBinding binding;
    private MortageViewModel viewModel;
    private String customerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatingMortageAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        customerId = getIntent().getStringExtra("customerId");
        AppDatabase db = Room.databaseBuilder(
                this,
                AppDatabase.class,
                "customer_db"
        ).allowMainThreadQueries().build();

        MortageRepository repo =
                new MortageRepository(
                        db.mortageDao(),
                        db.mortagePaymentDao(),
                        db.customerDao(),
                        db.transactionDao(),
                        new TransactionRepository(db.transactionDao()),
                        new CustomerRepository(this)
                );


        viewModel = new MortageViewModel(repo, new CustomerRepository(this));


        binding.createLoanAccount.setOnClickListener(v->createMortage());


    }

    private void createMortage(){
        double principal = Double.parseDouble(binding.edtS.getText().toString());
        double interestRate  = Double.parseDouble(binding.edtR.getText().toString());
        int termMonths = Integer.parseInt(binding.edtN.getText().toString());

        MortageEntity mortage = new MortageEntity(
                "",
                customerId,
                "VAY",
                principal,
                interestRate,
                termMonths,
                0,
                0,
                principal

        );
        viewModel.createMortage(mortage);

        finish();

    }
}
