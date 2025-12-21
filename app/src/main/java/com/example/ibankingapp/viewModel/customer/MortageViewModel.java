package com.example.ibankingapp.viewModel.customer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.ibankingapp.entity.CustomerEntity;
import com.example.ibankingapp.entity.MortageEntity;
import com.example.ibankingapp.entity.MortagePaymentEntity;
import com.example.ibankingapp.model.Customer;
import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.repository.MortageRepository;
import com.google.firebase.auth.FirebaseAuth;

public class MortageViewModel extends ViewModel {
    private final MortageRepository repository;
    private final CustomerRepository customerRepository;
    private final LiveData<Customer> currentCustomer;



    public MortageViewModel(MortageRepository repository, CustomerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentCustomer = customerRepository.getCustomerByUid(uid);

    }
    public LiveData<Customer> getCurrentCustomer() {
        return currentCustomer;
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
    public void payCurrentPeriod(MortagePaymentEntity payment, MortageEntity mortage, CustomerEntity customer, String accountNumber) {
        repository.payCurrentPeriod(payment, mortage, customer, accountNumber);
    }

}
