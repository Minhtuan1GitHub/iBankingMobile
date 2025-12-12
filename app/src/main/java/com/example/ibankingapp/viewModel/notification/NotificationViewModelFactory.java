package com.example.ibankingapp.viewModel.notification;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.ibankingapp.data.database.AppDatabase;
import com.example.ibankingapp.repository.NotificationRepository;

public class NotificationViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public NotificationViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(NotificationViewModel.class)) {
            NotificationRepository repo = new NotificationRepository(
                    AppDatabase.getInstance(application).notificationDao()
            );
            return (T) new NotificationViewModel(repo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

