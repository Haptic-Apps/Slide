package me.ccrama.redditslide.Notifications;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import me.ccrama.redditslide.Reddit;


public class NotificationJobScheduler   {
    private PendingIntent pendingIntent;

    public NotificationJobScheduler(Application context) {
        Intent alarmIntent = new Intent(context, CheckForMail.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        start(context);
    }

    public void start(Context c) {
        AlarmManager manager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        int interval = 1000 * 60 * Reddit.notificationTime;
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);

    }

    public void cancel(Context c) {
        AlarmManager manager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }


}