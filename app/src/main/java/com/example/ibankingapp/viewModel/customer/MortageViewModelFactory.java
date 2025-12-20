package com.example.ibankingapp.viewModel.customer;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.repository.MortageRepository;

public class MortageViewModelFactory implements ViewModelProvider.Factory {
    private final MortageRepository repository;

    public MortageViewModelFactory(MortageRepository repository) {
        this.repository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MortageViewModel.class)) {
            return (T) new MortageViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}