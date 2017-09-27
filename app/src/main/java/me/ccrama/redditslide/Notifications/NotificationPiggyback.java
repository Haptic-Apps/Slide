package me.ccrama.redditslide.Notifications;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by Carlos on 9/27/2017.
 */

public class NotificationPiggyback extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType()
                == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            final String packagename = String.valueOf(accessibilityEvent.getPackageName());
            if (packagename.equals("com.reddit.frontpage")) {
                Intent alarmIntent = new Intent(getApplicationContext(), CheckForMailSingle.class);
                PendingIntent pendingIntent =
                        PendingIntent.getBroadcast(getApplicationContext(), 0, alarmIntent, 0);
                AlarmManager manager =
                        (AlarmManager) getApplication().getSystemService(Context.ALARM_SERVICE);
                manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100,
                        pendingIntent);
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        setServiceInfo(info);
    }

    @Override
    public void onInterrupt() {
    }

}
