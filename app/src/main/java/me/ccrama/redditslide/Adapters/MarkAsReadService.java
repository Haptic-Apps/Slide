package me.ccrama.redditslide.Adapters;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.InboxManager;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Notifications.CheckForMail;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by brent on 1/27/16.
 */
public class MarkAsReadService extends IntentService {


    public MarkAsReadService() {
        super("MarkReadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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

            NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancel(0);
        }

    }

}
