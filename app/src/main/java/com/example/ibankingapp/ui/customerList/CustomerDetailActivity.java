package com.example.ibankingapp.ui.customerList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityCustomerDetailBinding;
import com.example.ibankingapp.entity.SavingAccountEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.SavingAccountRepository;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModel;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModelFactory;

public class CustomerDetailActivity extends AppCompatActivity {

    private ActivityCustomerDetailBinding customerDetailBinding;
    private CustomerViewModel viewModelCustomerDetail;
    private Customer currentCustomer;
    private SavingAccountEntity currentSavingAccount;
    private SavingAccountViewModel viewModelSavingAccount;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customerDetailBinding = ActivityCustomerDetailBinding.inflate(getLayoutInflater());
        setContentView(customerDetailBinding.getRoot());

        viewModelCustomerDetail = new ViewModelProvider(this).get(CustomerViewModel.class);

        AppDatabase db = AppDatabase.getInstance(this);
        SavingAccountRepository repo = new SavingAccountRepository(db.savingAccountDao());
        SavingAccountViewModelFactory fac = new SavingAccountViewModelFactory(repo);
        viewModelSavingAccount = new ViewModelProvider(this, fac).get(SavingAccountViewModel.class);


        String accountNumberCustomer = getIntent().getStringExtra("accountNumber");
//        String customerId = getIntent().getStringExtra("customerId");

        if (accountNumberCustomer == null){
            Toast.makeText(this, "Không tìm thấy khách hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModelCustomerDetail.getCustomerByAccountNumber(accountNumberCustomer).observe(this, customer ->{
            if (customer == null) {
                return;
            }
            currentCustomer = customer;
            customerDetailBinding.edtName.setText(currentCustomer.getFullName());
            customerDetailBinding.edtPhone.setText(currentCustomer.getPhone());
            customerDetailBinding.tbBalance.setText(String.valueOf(currentCustomer.getBalance()));
            customerDetailBinding.tbAccountNumberValue.setText(currentCustomer.getAccountNumber());
            customerDetailBinding.tbAccountType.setText(currentCustomer.getAccountType());
            customerDetailBinding.edtOtp.setText(currentCustomer.getOtp());

            viewModelCustomerDetail
                    .hasSavingAccount(customer.getId())
                    .observe(this, hasAccount -> {
                        if (hasAccount != null && hasAccount) {
                            customerDetailBinding.createSavingAccount.setEnabled(false);
                            customerDetailBinding.createSavingAccount.setAlpha(0.4f);
                            customerDetailBinding.createSavingAccount.setText("ĐÃ CÓ TÀI KHOẢN TIẾT KIỆM");
                        }
                    });
            viewModelSavingAccount.syncFromFirestore(customer.getId());
            viewModelSavingAccount
                    .getSavingAccounts(customer.getId())
                    .observe(this, savingAccount -> {

                        currentSavingAccount = savingAccount;

                        if (savingAccount == null) {
                            // Chưa có tài khoản tiết kiệm
                            customerDetailBinding.tbBalance1.setText("0");
                            customerDetailBinding.edtLaiSuat.setText("0");
                            customerDetailBinding.edtKyHan.setText("0");
                            customerDetailBinding.createSavingAccount.setVisibility(View.VISIBLE);
                            return;
                        }

                        customerDetailBinding.tbBalance1.setText(
                                String.valueOf(savingAccount.getBalance())
                        );
                        customerDetailBinding.edtLaiSuat.setText(
                                String.valueOf(savingAccount.getInterestRate())
                        );
                        customerDetailBinding.edtKyHan.setText(
                                String.valueOf(savingAccount.getTermMonths())
                        );

                    });


        });


        customerDetailBinding.btnSave.setOnClickListener(v->updateCustomer());

        customerDetailBinding.createSavingAccount.setOnClickListener(v->createSavingAccount());


    }

    private void updateCustomer() {
        if (currentCustomer == null) {
            Toast.makeText(this, "Chưa load xong dữ liệu khách hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = customerDetailBinding.edtName.getText().toString();
        String phone = customerDetailBinding.edtPhone.getText().toString();
        String otp = customerDetailBinding.edtOtp.getText().toString();



        currentCustomer.setFullName(name);
        currentCustomer.setPhone(phone);
        currentCustomer.setOtp(otp);


        viewModelCustomerDetail.updateCustomer(currentCustomer);

        if (currentSavingAccount == null) {
            Toast.makeText(this, "Chưa load xong dữ liệu khách hàng!!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSavingAccount.setInterestRate(Double.parseDouble(customerDetailBinding.edtLaiSuat.getText().toString()));
        //currentSavingAccount.setTermMonths(Integer.parseInt(customerDetailBinding.edtKyHan.getText().toString()));
       // currentSavingAccount.setTermMonths(customerDetailBinding.edtKyHan.getText().toString());
        Long term = Long.parseLong(customerDetailBinding.edtKyHan.getText().toString());
        currentSavingAccount.setTermMonths(term);

        viewModelSavingAccount.updateSavingAccount(currentSavingAccount);


        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void createSavingAccount() {
        Intent intent = new Intent(this, CreateSavingAccountActivity.class);
        intent.putExtra("customerId", currentCustomer.getId());
        startActivity(intent);
    }


}
