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
    public LiveData<List<NotificationEntity>> getNotifications(){
        return dao.getNotifications();
    }
    public LiveData<Integer> getUnreadCount(){
        return dao.getUnreadNotificationsCount();
    }
    public void markAsRead(){
        Executors.newSingleThreadExecutor().execute(dao::markNotificationsAsRead);
    }


}
