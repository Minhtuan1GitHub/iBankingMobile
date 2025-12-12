package com.example.ibankingapp.viewModel.customer;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.repository.SavingAccountRepository;

public class SavingAccountViewModelFactory implements ViewModelProvider.Factory {

    private final SavingAccountRepository repository;

    public SavingAccountViewModelFactory(SavingAccountRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SavingAccountViewModel.class)) {
            return (T) new SavingAccountViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
