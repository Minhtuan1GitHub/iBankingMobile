package com.example.ibankingapp.viewModel.customer;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.entity.SavingAccountEntity;
import com.example.ibankingapp.repository.SavingAccountRepository;

import java.util.List;

import kotlinx.coroutines.Dispatchers;

public class SavingAccountViewModel extends ViewModel {
    private final SavingAccountRepository repository;

    public SavingAccountViewModel(SavingAccountRepository repo){
        this.repository = repo;
    }

    public LiveData<SavingAccountEntity> getSavingAccounts(String customerId){
        return repository.getSavingAccounts(customerId);
    }

    public void createSavingAccount(SavingAccountEntity acc) {
        repository.createSavingAccount(acc);
    }

    public void updateSavingAccount(SavingAccountEntity acc){
        repository.updateSavingAccount(acc);
    }

    public void syncFromFirestore(String customerId){
        repository.syncFromFirestore(customerId);
    }
}

