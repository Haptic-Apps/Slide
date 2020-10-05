package me.ccrama.redditslide.Autocache;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import java.util.Calendar;

import me.ccrama.redditslide.Reddit;

/**
 * Created by carlo_000 on 10/13/2015.
 */


public class AutoCacheScheduler {
    private final PendingIntent pendingIntent;

    public AutoCacheScheduler(Context context) {
        Intent alarmIntent = new Intent(context, CacheAll.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        start(context);
    }

    public void start(Context c) {

        AlarmManager manager = ContextCompat.getSystemService(c, AlarmManager.class);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, Reddit.cachedData.getInt("hour", 0));
        cal.set(Calendar.MINUTE, Reddit.cachedData.getInt("minute", 0));

        if(cal.getTimeInMillis() <System.currentTimeMillis()){
            cal.set(Calendar.DAY_OF_YEAR , cal.get(Calendar.DAY_OF_YEAR) + 1);
        }
        if (manager != null) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public void cancel(Context c) {
        AlarmManager manager = ContextCompat.getSystemService(c, AlarmManager.class);
        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }


}
