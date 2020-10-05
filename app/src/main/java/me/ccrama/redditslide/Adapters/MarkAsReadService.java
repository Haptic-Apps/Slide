package me.ccrama.redditslide.Adapters;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.InboxManager;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Notifications.CheckForMail;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by brent on 1/27/16.
 */
public class MarkAsReadService extends IntentService {

    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    public MarkAsReadService() {
        super("MarkReadService");
    }

    public static PendingIntent getMarkAsReadIntent(int notificationId, Context context, String[] messageNames) {
        Intent intent = new Intent(context, MarkAsReadService.class);
        intent.putExtra(NOTIFICATION_ID, notificationId - 2);
        intent.putExtra(CheckForMail.MESSAGE_EXTRA, messageNames);
        return PendingIntent.getService(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager manager = ContextCompat.getSystemService(this, NotificationManager.class);
        if (manager != null) {
            manager.cancel(intent.getIntExtra(NOTIFICATION_ID, -1));
        }

        String[] messages = null;
        Bundle extras = intent.getExtras();
        if (extras != null) messages = extras.getStringArray(CheckForMail.MESSAGE_EXTRA);

        InboxManager inboxManager = new InboxManager(Authentication.reddit);

        if (messages != null && NetworkUtil.isConnected(getBaseContext())) {
            for (String message : messages) {
                try {
                    inboxManager.setRead(message, true);
                } catch (NetworkException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

    }

}
