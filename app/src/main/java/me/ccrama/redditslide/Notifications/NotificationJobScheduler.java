package me.ccrama.redditslide.Notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import me.ccrama.redditslide.Reddit;

/**
 * Created by carlo_000 on 10/13/2015.
 */


public class NotificationJobScheduler {
    private final PendingIntent pendingIntent;

    public NotificationJobScheduler(Context context) {
        Intent alarmIntent = new Intent(context, CheckForMail.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        start(context);
    }

    public void start(Context c) {
        AlarmManager manager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        int interval = 1000 * 60 * Reddit.notificationTime;
        long currentTime = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, currentTime + interval,
                    pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, currentTime + interval, pendingIntent);
        }
    }

    public void cancel(Context c) {
        AlarmManager manager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }
}