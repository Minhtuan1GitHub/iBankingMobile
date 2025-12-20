package com.example.ibankingapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.example.ibankingapp.R;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "transfer_channel";
    private static final String CHANNEL_NAME = "Giao dịch ngân hàng";

    public static void send(Context context, String title, String message) {
        Log.d(TAG, "send() - Bắt đầu gửi notification");
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Message: " + message);

        // Kiểm tra permission cho Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Không có quyền POST_NOTIFICATIONS!");
                return;
            }
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            Log.e(TAG, "NotificationManager is NULL!");
            return;
        }

        // Tạo Notification Channel cho Android O+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo về các giao dịch ngân hàng");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setShowBadge(true);

            manager.createNotificationChannel(channel);
            Log.d(TAG, "Notification Channel đã được tạo");
        }

        // Tạo notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notify) // Icon notification từ drawable
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Hiển thị full text
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // Priority cao
                        .setAutoCancel(true) // Tự động xóa khi click
                        .setDefaults(NotificationCompat.DEFAULT_ALL); // Sound, vibration, lights

        // Gửi notification
        int notificationId = (int) System.currentTimeMillis();
        try {
            manager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification đã được gửi thành công! ID: " + notificationId);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi gửi notification: " + e.getMessage(), e);
        }
    }
}
