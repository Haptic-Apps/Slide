package me.ccrama.redditslide.Notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.AlarmManagerCompat;
import androidx.core.content.ContextCompat;

import me.ccrama.redditslide.Reddit;

/**
 * Created by carlo_000 on 10/13/2015.
 */


public class NotificationJobScheduler {
    private final PendingIntent pendingIntent;
    private final AlarmManager manager;

    public NotificationJobScheduler(Context context) {
        final Intent alarmIntent = new Intent(context, CheckForMail.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        manager = ContextCompat.getSystemService(context, AlarmManager.class);
        start();
    }

    public void start() {
        final int interval = 1000 * 60 * Reddit.notificationTime;
        final long currentTime = System.currentTimeMillis();
        if (manager != null) {
            AlarmManagerCompat.setAndAllowWhileIdle(
                    manager, AlarmManager.RTC_WAKEUP, currentTime + interval, pendingIntent);
        }
    }

    public void cancel() {
        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }
}
