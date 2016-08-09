package me.ccrama.redditslide.Notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by carlo_000 on 10/13/2015.
 */


public class SubPostScheduler {
    private final PendingIntent pendingIntent;

    public SubPostScheduler(Context context) {
        Intent alarmIntent = new Intent(context, CheckSubs.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        start(context);
    }

    public void start(Context c) {
        AlarmManager manager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        int interval = 600000; //10 minutes
        long currentTime = System.currentTimeMillis();
        manager.set(AlarmManager.RTC_WAKEUP, currentTime + interval, pendingIntent);
    }

    public void cancel(Context c) {
        AlarmManager manager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }
}