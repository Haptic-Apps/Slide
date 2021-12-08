package me.ccrama.redditslide.Notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

/**
 * Created by Carlos on 9/27/2017.
 */
public class NotificationPiggyback extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();
        if (!TextUtils.isEmpty(packageName) && packageName.equals("com.reddit.frontpage")) {
            cancelNotification(sbn.getKey());
            Intent alarmIntent = new Intent(getApplicationContext(), CheckForMailSingle.class);
            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, 0);
            AlarmManager manager =
                    ContextCompat.getSystemService(getApplication(), AlarmManager.class);
            if (manager != null) {
                manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, pendingIntent);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Nothing to do
    }
}

