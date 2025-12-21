package com.example.ibankingapp.repository;

import androidx.lifecycle.LiveData;

import com.example.ibankingapp.data.dao.NotificationDao;
import com.example.ibankingapp.entity.NotificationEntity;

import java.util.List;
import java.util.concurrent.Executors;

public class NotificationRepository {
    private final NotificationDao dao;

    public NotificationRepository(NotificationDao dao){
        this.dao = dao;
    }

    public void addNotification(NotificationEntity notification){
        Executors.newSingleThreadExecutor().execute(()->dao.insert(notification));
    }
    public LiveData<List<NotificationEntity>> getNotifications(String customerId){
        return dao.getNotifications(customerId);
    }
    public LiveData<Integer> getUnreadCount(String customerId){
        return dao.getUnreadNotificationsCount(customerId);
    }
    public void markAsRead(String customerId){
        Executors.newSingleThreadExecutor().execute(() -> dao.markNotificationsAsRead(customerId));
    }


}
