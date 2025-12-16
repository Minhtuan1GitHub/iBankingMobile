package com.example.ibankingapp.viewModel.transaction;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.entity.TransactionEntity;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.repository.TransactionRepository;
import com.example.ibankingapp.utils.TransactionDisplay;

import java.util.ArrayList;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionRepository repository;
    private final MutableLiveData<List<TransactionDisplay>> transactions = new MutableLiveData<>();
    private final CustomerRepository customerRepository;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new TransactionRepository(db.transactionDao());
        customerRepository = new CustomerRepository(application);
    }


    public LiveData<List<TransactionDisplay>> getTransactions() {
        return transactions;
    }

    public void loadTransactions(String accountNumber) {

        // xoa du lieu cu
        transactions.postValue(new ArrayList<>());

        repository.getAllTransactions(accountNumber, transactionList -> {
            List<TransactionDisplay> displayList = new ArrayList<>();
            for (TransactionEntity t : transactionList) {
                CustomerEntity c = customerRepository.getCustomerByAccount(t.getToAcountNumber()); // trả CustomerEntity
                String name = (c != null) ? c.getFullName() : t.getToAcountNumber();
                displayList.add(new TransactionDisplay(t, name));
            }
            transactions.postValue(displayList); // hợp lệ với MutableLiveData<List<TransactionDisplay>>
        });
    }



}
