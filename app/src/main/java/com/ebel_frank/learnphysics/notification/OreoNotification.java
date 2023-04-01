package com.ebel_frank.learnphysics.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

class OreoNotification extends ContextWrapper {

    private static final String CHANNEL_ID = "com.ebel_frank.learnphysics.notification.channel_id";
    private static final String CHANNEL_NAME = "com.ebel_frank.learnphysics.notification.channel_name";

    private NotificationManager notificationManager;

    public OreoNotification(Context context) {
        super(context);
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableVibration(true);
        notificationChannel.enableLights(true);
        notificationChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @TargetApi (Build.VERSION_CODES.O)
    public Notification.Builder getNotifications(String title, String body,
                                                 PendingIntent pending,
                                                 Uri soundUri, String icon) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pending)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(Integer.parseInt(icon));
    }
}

