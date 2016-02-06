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
import android.util.Log;

import net.dean.jraw.models.Message;
import net.dean.jraw.paginators.InboxPaginator;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.Inbox;
import me.ccrama.redditslide.Adapters.MarkAsReadService;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

public class CheckForMail extends BroadcastReceiver {

    public static final String MESSAGE_EXTRA = "MESSAGE_FULLNAMES";
    private Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        if (NetworkUtil.isConnected(c)) {
            new AsyncGetMail().execute();
            Log.v(LogUtil.getTag(), "CHECKING MAIL");
        }
    }


    private class AsyncGetMail extends AsyncTask<Void, Void, List<Message>> {

        ArrayList<Message> modMessages = new ArrayList<>();

        @Override
        public void onPostExecute(List<Message> messages) {
            Resources res = c.getResources();
            if (messages != null && messages.size() > 0) {

                //create arraylist of the messages fullName for markasread action
                String[] messageNames = new String[messages.size()];
                int counter = 0;
                for (Message x : messages) {
                    messageNames[counter] = x.getFullName();
                    counter++;
                }

                NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);


                Intent notificationIntent = new Intent(c, Inbox.class);

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
                            .addAction(R.drawable.ic_check_all_black, c.getString(R.string.misc_mark_read), readPI)
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
                            .addAction(R.drawable.ic_check_all_black, c.getString(R.string.misc_mark_read), readPI)
                            .build();
                    notificationManager.notify(0, notification);
                }
            }
            /*todoif(modMessages != null && modMessages.size() > 0){
                    if (modMessages.size() == 1) {
                        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);


                        Intent notificationIntent = new Intent(c, ModQueue.class);

                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        PendingIntent intent = PendingIntent.getActivity(c, 0,
                                notificationIntent, 0);

                        NotificationCompat.BigTextStyle notiStyle = new
                                NotificationCompat.BigTextStyle();
                        notiStyle.setBigContentTitle(c.getString(R.string.modmail_notification_msg, modMessages.get(0).getAuthor()));
                        notiStyle.bigText(Html.fromHtml(modMessages.get(0).getBody()));

                        String name;
                        if(modMessages.get(0).getSubreddit() ==null || modMessages.get(0).getSubreddit().isEmpty()){
                            name = modMessages.get(0).getAuthor();
                        } else {
                            name = modMessages.get(0).getSubreddit();
                        }
                        Notification notification = new NotificationCompat.Builder(c).setContentIntent(intent)
                                .setSmallIcon(R.drawable.mod)
                                .setTicker(res.getQuantityString(R.plurals.modmail_notification_title, 1, 1))
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(true)
                                .setContentTitle(c.getString(R.string.mail_notification_author,
                                        messages.get(0).getSubject(), name))
                                .setContentText(Html.fromHtml(messages.get(0).getBody()))
                                .setStyle(notiStyle)
                                .build();
                        notificationManager.notify(1, notification);
                    } else {
                        int amount = messages.size();
                        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

                        NotificationCompat.InboxStyle notiStyle = new
                                NotificationCompat.InboxStyle();
                        notiStyle.setBigContentTitle(res.getQuantityString(R.plurals.modmail_notification_title, amount, amount));
                        notiStyle.setSummaryText("");
                        for (Message m : messages) {
                            String name;
                            if(m.getSubreddit() == null || m.getSubreddit().isEmpty() ){
                                name =m.getAuthor();
                            } else {
                                name = m.getSubreddit();
                            }
                            notiStyle.addLine(c.getString(R.string.modmail_notification_msg, name));
                        }

                        Intent notificationIntent = new Intent(c, ModQueue.class);

                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        PendingIntent intent = PendingIntent.getActivity(c, 0,
                                notificationIntent, 0);


                        Notification notification = new NotificationCompat.Builder(c)
                                .setContentIntent(intent)
                                .setSmallIcon(R.drawable.mod)
                                .setTicker(res.getQuantityString(R.plurals.mail_notification_title, amount, amount))
                                .setWhen(System.currentTimeMillis())
                                .setAutoCancel(true)
                                .setContentTitle(res.getQuantityString(R.plurals.modmail_notification_title, amount, amount))
                                .setStyle(notiStyle)
                                .build();
                        notificationManager.notify(1, notification);

                }
            }*/
        }

        @Override
        protected List<Message> doInBackground(Void... params) {
            try {
                if (Authentication.isLoggedIn && Authentication.didOnline) {
                    InboxPaginator unread = new InboxPaginator(Authentication.reddit, "unread");

                    ArrayList<Message> messages = new ArrayList<>();
                    if (unread.hasNext()) {
                        messages.addAll(unread.next());
                    }

                   /*todo if(Authentication.mod) {
                        modMessages =new ArrayList<>();
                        InboxPaginator mod = new InboxPaginator(Authentication.reddit, "moderator/unread");
                        if (mod.hasNext()) {
                            modMessages.addAll(mod.next());
                        }

                    }*/

                    return messages;
                }
            } catch (Exception ignored) {

                ignored.printStackTrace();
            }
            return null;
        }
    }


}