package com.example.ibankingapp.viewModel.customer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.entity.MortageEntity;
import com.example.ibankingapp.entity.MortagePaymentEntity;
import com.example.ibankingapp.repository.MortageRepository;

public class MortageViewModel extends ViewModel {
    private final MortageRepository repository;


    public MortageViewModel(MortageRepository repository) {
        this.repository = repository;
    }

    public void createMortage(MortageEntity mortage) {
        repository.createMortage(mortage);
    }
    public LiveData<MortageEntity> getMortageByCustomerId(String customerId) {
        return repository.getMortageByCustomerId(customerId);
    }
    public void syncFromFirestore(String customerId) {
        repository.syncFromFirestore(customerId);
    }
    public void updateMortage(MortageEntity mortage) {
        repository.updateMortage(mortage);
    }
    public LiveData<MortagePaymentEntity> getCurrentPayment(String mortgageId) {
        return repository.getCurrentPayment(mortgageId);
    }
    public void payCurrentPeriod(MortagePaymentEntity payment, MortageEntity mortage, CustomerEntity customer) {
        repository.payCurrentPeriod(payment, mortage, customer);
    }

}
