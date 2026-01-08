package com.example.tastelandv1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "food_expiry_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Get the food name passed from AddFood.java
        String foodName = intent.getStringExtra("food_name");

        // 2. Create the Notification Manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 3. Create the Notification Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Food Expiry Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // 4. Build the actual notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bell) // Ensure this icon exists in res/drawable
                .setContentTitle("Food Reminder")
                .setContentText("Don't forget: " + foodName + " is expiring soon!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 5. Show it
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}