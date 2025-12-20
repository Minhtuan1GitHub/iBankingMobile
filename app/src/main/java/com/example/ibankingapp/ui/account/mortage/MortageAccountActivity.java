package com.example.ibankingapp.ui.account.mortage;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.data.dao.CustomerDao;
import com.example.ibankingapp.data.dao.MortageDao;
import com.example.ibankingapp.data.dao.MortagePaymentDao;
import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.databinding.ActivityLoanBinding;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.entity.MortageEntity;
import com.example.ibankingapp.entity.MortagePaymentEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.MortageRepository;
import com.example.ibankingapp.viewModel.customer.CustomerViewModel;
import com.example.ibankingapp.viewModel.customer.MortageViewModel;
import com.example.ibankingapp.viewModel.customer.SavingAccountViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MortageAccountActivity extends AppCompatActivity {
    private ActivityLoanBinding binding;
    private MortageViewModel viewModel;
    private CustomerViewModel customerViewModel;
    private MortagePaymentEntity currentPayment;
    private MortageEntity currentMortage;
    private CustomerEntity currentCustomer;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MortageDao dao = AppDatabase.getInstance(this).mortageDao();
        MortagePaymentDao paymentDao = AppDatabase.getInstance(this).mortagePaymentDao();
        CustomerDao customerDao = AppDatabase.getInstance(this).customerDao();

        MortageRepository repo = new MortageRepository(dao, paymentDao, customerDao);

        viewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.Factory() {
                    @Override
                    public <T extends ViewModel> T create(Class<T> modelClass) {
                        return (T) new MortageViewModel(repo);
                    }
                }
        ).get(MortageViewModel.class);
        customerViewModel = new ViewModelProvider(this).get(CustomerViewModel.class);

        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.syncFromFirestore(customerId);
        viewModel.getMortageByCustomerId(customerId)
                .observe(this, mortage -> {
                    if (mortage == null) return;
                    currentMortage = mortage;

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    binding.balanceValue.setText(String.valueOf(mortage.getRemainingBalance()));
                    String nextPayDate = sdf.format(mortage.getNextPaymentDate());
                    binding.dueDateValue.setText(nextPayDate);
                    binding.balanceValueRoot.setText(String.valueOf(mortage.getPrincipal()));
                    binding.interestRateValue.setText(String.valueOf(mortage.getInterestRate()));


                    String dateStr = sdf.format(mortage.getCreatedAt());

                    binding.termValue.setText(dateStr);


                });

        viewModel.getCurrentPayment(customerId)
                .observe(this, payment->{
                    if (payment == null) return;

                    currentPayment = payment;

                    binding.paymentAmountValue.setText(String.valueOf(payment.getAmount()));
                    binding.paymentStatusValue.setText(payment.getStatus());

                    if (payment.getStatus().equals("UNPAID")){
                        binding.layoutPayment.setVisibility(View.VISIBLE);

                    }else{
                        binding.layoutPayment.setVisibility(View.GONE);
                    }

                });

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        customerViewModel.getCustomer(uid)
                .observe(this, customer -> {
                    if (customer == null) return;

                    currentCustomer = toEntity(customer); // ⭐ CONVERT TẠI ĐÂY

                    binding.tvName.setText("Họ và tên: " + customer.getFullName());
                    binding.tvBalance.setText("Số dư hiện tại: " + customer.getBalance());
                });




        binding.btnPay.setOnClickListener(v-> payment());
    }
    private void payment() {
        if (!canPay(currentPayment)) {
            Toast.makeText(
                    this,
                    "Chỉ được thanh toán trong vòng 5 ngày trước hạn",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        showPin(uid);
    }

    private void showPin(String uid){
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
        viewModel.payCurrentPeriod(currentPayment, currentMortage, currentCustomer);
        Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
    }

    private boolean canPay(MortagePaymentEntity payment) {
        long now = System.currentTimeMillis();
        long dueDate = payment.getDueDate();

        long diffMillis = dueDate - now;
        long diffDays = diffMillis / (1000 * 60 * 60 * 24);

        return diffDays >= 0 && diffDays <= 5;
    }
    private CustomerEntity toEntity(Customer c) {
        CustomerEntity e = new CustomerEntity();
        e.setId(c.getId());
        e.setFullName(c.getFullName());
        e.setBalance(c.getBalance());
        return e;
    }







}
