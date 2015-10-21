package me.ccrama.redditslide.Notifications;

/**
 * Created by carlo_000 on 10/13/2015.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.text.Html;

import net.dean.jraw.models.Message;
import net.dean.jraw.paginators.InboxPaginator;

import java.util.List;

import me.ccrama.redditslide.Activities.Inbox;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;

public class CheckForMail extends BroadcastReceiver {

    Context c;
    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        new AsyncGetMail().execute();
        }
    public class AsyncGetMail extends AsyncTask<Void, Void, List<Message>> {

        @Override
        public void onPostExecute(List<Message> messages){
            Resources res = c.getResources();
            if(messages != null && messages.size() > 0) {
                if (messages.size() == 1) {
                    NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);


                    Intent notificationIntent = new Intent(c, Inbox.class);

                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent intent = PendingIntent.getActivity(c, 0,
                            notificationIntent, 0);

                    NotificationCompat.BigTextStyle notiStyle = new
                            NotificationCompat.BigTextStyle();
                    notiStyle.setBigContentTitle(c.getString(R.string.mail_notification_msg) + " " + messages.get(0).getAuthor());
                    notiStyle.bigText(Html.fromHtml(messages.get(0).getBody()));

                    Notification notification = new NotificationCompat.Builder(c).setContentIntent(intent)
                            .setSmallIcon(R.drawable.notif)
                            .setTicker(res.getQuantityString(R.plurals.mail_notification_title, 1))
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setContentTitle(messages.get(0).getSubject() + " " +
                                    c.getString(R.string.mail_notification_author) + " " + messages.get(0).getAuthor())
                            .setContentText(Html.fromHtml(messages.get(0).getBody()))
                            .setStyle(notiStyle)
                            .build();
                    notificationManager.notify(0, notification);
                } else {
                    int amount = messages.size();
                    NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

                    NotificationCompat.InboxStyle notiStyle = new
                            NotificationCompat.InboxStyle();
                    notiStyle.setBigContentTitle(res.getQuantityString(R.plurals.mail_notification_title, amount, amount));
                    notiStyle.setSummaryText("");
                    for(Message m : messages) {
                        notiStyle.addLine(c.getString(R.string.mail_notification_msg) + " " + m.getAuthor());
                    }

                    Intent notificationIntent = new Intent(c, Inbox.class);

                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent intent = PendingIntent.getActivity(c, 0,
                            notificationIntent, 0);


                    Notification notification = new NotificationCompat.Builder(c)
                            .setContentIntent(intent)
                            .setSmallIcon(R.drawable.notif)
                            .setTicker(res.getQuantityString(R.plurals.mail_notification_title, amount, amount))
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setContentTitle(res.getQuantityString(R.plurals.mail_notification_title, amount, amount))
                            .setStyle(notiStyle)
                            .build();
                    notificationManager.notify(0, notification);
                }
            }
        }
        @Override
        protected List<Message> doInBackground(Void... params) {
            try {
                if (Authentication.isLoggedIn) {
                    InboxPaginator unread = new InboxPaginator(Authentication.reddit, "unread");
                    if (unread.hasNext()) {
                        return unread.next();
                    }
                }
            }catch(Exception e){

            }
            return null;
        }
    }
}