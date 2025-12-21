package com.example.ibankingapp.viewModel.customer;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.repository.CustomerRepository;
import com.example.ibankingapp.repository.MortageRepository;

public class MortageViewModelFactory implements ViewModelProvider.Factory {
    private final MortageRepository repository;
    private final CustomerRepository customerRepository;


    public MortageViewModelFactory(MortageRepository repository, CustomerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MortageViewModel.class)) {
            return (T) new MortageViewModel(repository, customerRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}