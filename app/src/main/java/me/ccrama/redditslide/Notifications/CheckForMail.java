package me.ccrama.redditslide.Notifications;

/**
 * Created by carlo_000 on 10/13/2015.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.text.Html;

import net.dean.jraw.models.Message;
import net.dean.jraw.paginators.InboxPaginator;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.Inbox;
import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Adapters.MarkAsReadService;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.util.NetworkUtil;

public class CheckForMail extends BroadcastReceiver {

    public static final String MESSAGE_EXTRA = "MESSAGE_FULLNAMES";
    private Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        if (NetworkUtil.isConnected(c)) {
            new AsyncGetMail().execute();
            if(Authentication.mod)
            new AsyncGetModmail().execute();
        }
    }


    private class AsyncGetMail extends AsyncTask<Void, Void, List<Message>> {

        @Override
        public void onPostExecute(List<Message> messages) {
            Resources res = c.getResources();
            if (messages != null && !messages.isEmpty()) {
                if(Reddit.isPackageInstalled(c, "com.teslacoilsw.notifier")) {
                    try {

                        ContentValues cv = new ContentValues();

                        cv.put("tag", "me.ccrama.redditslide/me.ccrama.redditslide.MainActivity");

                        cv.put("count", messages.size());

                        c.getContentResolver().insert(Uri
                                        .parse("content://com.teslacoilsw.notifier/unread_count"),
                                cv);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                //create arraylist of the messages fullName for markasread action
                String[] messageNames = new String[messages.size()];
                int counter = 0;
                for (Message x : messages) {
                    messageNames[counter] = x.getFullName();
                    counter++;
                }

                NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);


                Intent notificationIntent = new Intent(c, Inbox.class);
                notificationIntent.putExtra(Inbox.EXTRA_UNREAD, true);

                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent intent = PendingIntent.getActivity(c, 0,
                        notificationIntent, 0);

                //Intent for mark as read notification action
                Intent readIntent = new Intent(c, MarkAsReadService.class);
                readIntent.putExtra(MESSAGE_EXTRA, messageNames);
                PendingIntent readPI = PendingIntent.getService(c, 2, readIntent, PendingIntent.FLAG_UPDATE_CURRENT);


                if (messages.size() == 1) {

                    NotificationCompat.BigTextStyle notiStyle = new
                            NotificationCompat.BigTextStyle();
                    notiStyle.setBigContentTitle(c.getString(R.string.mail_notification_msg, messages.get(0).getAuthor()));
                    notiStyle.bigText(Html.fromHtml(messages.get(0).getBody()));

                    Notification notification = new NotificationCompat.Builder(c).setContentIntent(intent)
                            .setSmallIcon(R.drawable.notif)
                            .setTicker(res.getQuantityString(R.plurals.mail_notification_title, 1, 1))
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setContentTitle(c.getString(R.string.mail_notification_author,
                                    messages.get(0).getSubject(), messages.get(0).getAuthor()))
                            .setContentText(Html.fromHtml(messages.get(0).getBody()))
                            .setStyle(notiStyle)
                            .addAction(R.drawable.ic_check_all_black, c.getString(R.string.mail_mark_read), readPI)
                            .build();
                    notificationManager.notify(0, notification);
                } else {
                    int amount = messages.size();

                    NotificationCompat.InboxStyle notiStyle = new
                            NotificationCompat.InboxStyle();
                    notiStyle.setBigContentTitle(res.getQuantityString(R.plurals.mail_notification_title, amount, amount));
                    notiStyle.setSummaryText("");
                    for (Message m : messages) {
                        notiStyle.addLine(c.getString(R.string.mail_notification_msg, m.getAuthor()));
                    }

                    Notification notification = new NotificationCompat.Builder(c)
                            .setContentIntent(intent)
                            .setSmallIcon(R.drawable.notif)
                            .setTicker(res.getQuantityString(R.plurals.mail_notification_title, amount, amount))
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setContentTitle(res.getQuantityString(R.plurals.mail_notification_title, amount, amount))
                            .setStyle(notiStyle)
                            .addAction(R.drawable.ic_check_all_black, c.getString(R.string.mail_mark_read), readPI)
                            .build();
                    notificationManager.notify(0, notification);
                }
            }
            new NotificationJobScheduler(c).start(c);
        }

        @Override
        protected List<Message> doInBackground(Void... params) {
            try {
                if (Authentication.isLoggedIn && Authentication.didOnline) {
                    InboxPaginator unread = new InboxPaginator(Authentication.reddit, "unread");

                    List<Message> messages = new ArrayList<>();
                    if (unread.hasNext()) {
                        messages.addAll(unread.next());
                    }

                    return messages;
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();

            }
            return null;
        }
    }

    private class AsyncGetModmail extends AsyncTask<Void, Void, List<Message>> {

        @Override
        public void onPostExecute(List<Message> messages) {
            Resources res = c.getResources();
            if (messages != null && !messages.isEmpty()) {

                NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

                Intent notificationIntent = new Intent(c, ModQueue.class);

                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent intent = PendingIntent.getActivity(c, 0,
                        notificationIntent, 0);

                if (messages.size() == 1) {

                    NotificationCompat.BigTextStyle notiStyle = new
                            NotificationCompat.BigTextStyle();
                    notiStyle.setBigContentTitle(c.getString(R.string.mod_mail_notification_msg, messages.get(0).getAuthor()));
                    notiStyle.bigText(Html.fromHtml(messages.get(0).getBody()));

                    Notification notification = new NotificationCompat.Builder(c).setContentIntent(intent)
                            .setSmallIcon(R.drawable.mod)
                            .setTicker(res.getQuantityString(R.plurals.mod_mail_notification_title, 1, 1))
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setContentTitle(c.getString(R.string.mail_notification_author,
                                    messages.get(0).getSubject(), messages.get(0).getAuthor()))
                            .setContentText(Html.fromHtml(messages.get(0).getBody()))
                            .setStyle(notiStyle)
                            .build();
                    notificationManager.notify(1, notification);
                } else {
                    int amount = messages.size();

                    NotificationCompat.InboxStyle notiStyle = new
                            NotificationCompat.InboxStyle();
                    notiStyle.setBigContentTitle(res.getQuantityString(R.plurals.mod_mail_notification_title, amount, amount));
                    notiStyle.setSummaryText("");
                    for (Message m : messages) {
                        notiStyle.addLine(c.getString(R.string.mod_mail_notification_msg, m.getAuthor()));
                    }

                    Notification notification = new NotificationCompat.Builder(c)
                            .setContentIntent(intent)
                            .setSmallIcon(R.drawable.mod)
                            .setTicker(res.getQuantityString(R.plurals.mod_mail_notification_title, amount, amount))
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setContentTitle(res.getQuantityString(R.plurals.mod_mail_notification_title, amount, amount))
                            .setStyle(notiStyle)
                            .build();
                    notificationManager.notify(1, notification);
                }
            }
            new NotificationJobScheduler(c).start(c);
        }

        @Override
        protected List<Message> doInBackground(Void... params) {
            try {
                if (Authentication.isLoggedIn && Authentication.didOnline) {
                    InboxPaginator unread = new InboxPaginator(Authentication.reddit, "moderator/unread");

                    List<Message> messages = new ArrayList<>();
                    if (unread.hasNext()) {
                        messages.addAll(unread.next());
                    }

                    return messages;
                }
            } catch (Exception ignored) {

                ignored.printStackTrace();
            }
            return null;
        }
    }

}