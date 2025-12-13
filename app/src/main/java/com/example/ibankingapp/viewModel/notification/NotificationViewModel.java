package com.example.ibankingapp.viewModel.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.ibankingapp.entity.NotificationEntity;
import com.example.ibankingapp.repository.NotificationRepository;

import java.util.List;

public class NotificationViewModel extends ViewModel {
    private final NotificationRepository repo;
    public NotificationViewModel(NotificationRepository repo){
        this.repo = repo;
    }
    public LiveData<List<NotificationEntity>> getNotifications(String customerId){
        return repo.getNotifications(customerId);
    }
    public LiveData<Integer> getUnreadCount(String customerId){
        return repo.getUnreadCount(customerId);
    }
    public void markAsRead(){
        repo.markAsRead();
    }

}
