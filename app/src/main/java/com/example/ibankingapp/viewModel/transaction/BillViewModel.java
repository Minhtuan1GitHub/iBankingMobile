package com.example.ibankingapp.viewModel.transaction;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.entity.BillEntity;
import com.example.ibankingapp.entity.NotificationEntity;
import com.example.ibankingapp.repository.BillRepository;
import com.example.ibankingapp.repository.NotificationRepository;
import com.example.ibankingapp.repository.TransactionRepository;
import com.google.firebase.auth.FirebaseAuth;

public class BillViewModel extends AndroidViewModel {
    private final BillRepository repository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;




    public BillViewModel(@NonNull Application application) {
        super(application);
        this.repository = new BillRepository();
        this.transactionRepository = new TransactionRepository(AppDatabase.getInstance(application).transactionDao());
        this.notificationRepository = new NotificationRepository(AppDatabase.getInstance(application).notificationDao());
    }

    public LiveData<BillEntity> findBill(String code){
        return repository.getBillByCode(code);
    }


    public LiveData<Boolean> payBill(
            BillEntity bill,
            String fromAccount,
            double balance
    ){
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        repository.payBill(bill, fromAccount, balance, new BillRepository.BillPaymentCallback() {
            @Override
            public void onResult(boolean success, String message) {
                if (success){
                    transactionRepository.logTransaction(
                            fromAccount,
                            "",
                            bill.getAmount(),
                            "Đã thanh toán",
                            "Thanh toán hóa đơn",
                            ""
                    );

                    NotificationEntity notification = new NotificationEntity();
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    notification.setCustomerId(uid);
                    notification.setTitle("Thanh toán hóa đơn");
                    notification.setMessage(
                            "Bạn đã thanh toán hóa đơn " + bill.getCustomerName()
                                    + " với số tiền " + bill.getAmount() + "đ từ tài khoản " + fromAccount
                    );
                    notification.setTimestamp(System.currentTimeMillis());
                    notification.setRead(false);

                    notificationRepository.addNotification(notification);

                }
                result.postValue(success);
            }

            @Override
            public void onFail(String message) {
                result.postValue(false);
                // Optional: log message hoặc show toast
            }
        });
        return result;
    }

}
