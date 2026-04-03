package com.example.myfirstapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    // Notification channel ID — must match what is
    // created in ItemAdapter when scheduling the alarm
    public static final String CHANNEL_ID = "listkeepr_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Extract item name and list name from the intent
        String itemName = intent.getStringExtra("item_name");
        String listName = intent.getStringExtra("list_name");

        if (itemName == null) itemName = "Reminder";
        if (listName == null) listName = "ListKeepr";

        // Create notification channel (required for Android 8+)
        createNotificationChannel(context);

        // Build and display the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_bell_filled)
                        .setContentTitle(listName)
                        .setContentText(itemName)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE);

        // Use the current time as a unique notification ID
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    // -----------------------------------------------
    // Creates the notification channel
    // Required for Android 8.0 (API 26) and above
    // Safe to call multiple times — system ignores
    // duplicate channel creation
    // -----------------------------------------------
    private void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "ListKeepr Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Reminders for your list items");

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(
                            Context.NOTIFICATION_SERVICE);

            manager.createNotificationChannel(channel);
        }
    }
}
