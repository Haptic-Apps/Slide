package me.ccrama.redditslide.Adapters;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import net.dean.jraw.managers.InboxManager;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;

/**
 * Created by brent on 1/27/16.
 */
public class MarkAsReadService extends IntentService {


    public MarkAsReadService() {
        super("MarkReadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String[] messages = intent.getExtras().getStringArray("MESSAGE_FULLNAMES");
        InboxManager inboxManager = new InboxManager(Authentication.reddit);
        for (String message : messages) {
            inboxManager.setRead(message, true);
        }


        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancel(0);

    }

}
