package com.example.ibankingapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ibankingapp.entity.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert
    void insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    LiveData<List<NotificationEntity>> getNotifications();

    @Query("SELECT COUNT(*) FROM notifications  WHERE  isRead = 0")
    LiveData<Integer> getUnreadNotificationsCount( );

    @Query("UPDATE notifications SET isRead = 1 ")
    void markNotificationsAsRead();
}
