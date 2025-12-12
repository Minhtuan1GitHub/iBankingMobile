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
    public LiveData<List<NotificationEntity>> getNotifications(){
        return repo.getNotifications();
    }
    public LiveData<Integer> getUnreadCount(){
        return repo.getUnreadCount();
    }
    public void markAsRead(){
        repo.markAsRead();
    }

}
