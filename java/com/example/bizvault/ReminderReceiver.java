package com.example.bizvault;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "document_expiry_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String docName = intent.getStringExtra("docName");
        int daysLeft = intent.getIntExtra("daysLeft", 0);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Document Expiry Alarms",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("High priority alarms for document expiry");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 1000});
            notificationManager.createNotificationChannel(channel);
        }

        String title;
        String message;

        if (daysLeft == 0) {
            title = "CRITICAL: Document Expires Today!";
            message = "Your document '" + docName + "' expires today. Take action now.";
        } else {
            title = "BizVault Pro: Expiry Reminder";
            message = "Reminder: Your document '" + docName + "' expires in " + daysLeft + " days.";
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(alarmSound)
                .setVibrate(new long[]{0, 500, 1000})
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}