package com.example.ibankingapp.viewModel.customer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.repository.InterbankTransferRepository;

public class InterbankTransferViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public InterbankTransferViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InterbankTransferViewModel.class)) {
            return (T) new InterbankTransferViewModel(
                    new InterbankTransferRepository(context)
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
