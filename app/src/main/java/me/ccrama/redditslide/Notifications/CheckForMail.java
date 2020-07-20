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
import android.text.Html;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import net.dean.jraw.models.Message;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.InboxPaginator;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Activities.CancelSubNotifs;
import me.ccrama.redditslide.Activities.Inbox;
import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Activities.OpenContent;
import me.ccrama.redditslide.Adapters.MarkAsReadService;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;

public class CheckForMail extends BroadcastReceiver {

    public static final String MESSAGE_EXTRA = "MESSAGE_FULLNAMES";
    public static final String SUBS_TO_GET   = "SUBREDDIT_NOTIFS";
    private Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        if (Authentication.reddit == null || !Authentication.reddit.isAuthenticated()) {
            Reddit.authentication = new Authentication(context);
        }

        if (!((Reddit) c.getApplicationContext()).isNotificationAccessEnabled()) {
            new AsyncGetMail().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (Authentication.mod) {
            new AsyncGetModmail().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (!Reddit.appRestart.getString(SUBS_TO_GET, "").isEmpty()) {
            new AsyncGetSubs(c).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        if (Reddit.notificationTime != -1) new NotificationJobScheduler(context).start(context);
    }


    private class AsyncGetMail extends AsyncTask<Void, Void, List<Message>> {

        @Override
        public void onPostExecute(List<Message> messages) {
            Resources res = c.getResources();
            if (messages != null && !messages.isEmpty()) {
                Collections.reverse(messages);
                if (Reddit.isPackageInstalled("com.teslacoilsw.notifier")) {
                    try {

                        ContentValues cv = new ContentValues();
                        cv.put("tag", "me.ccrama.redditslide/me.ccrama.redditslide.MainActivity");
                        cv.put("count", messages.size());
                        c.getContentResolver()
                                .insert(Uri.parse(
                                        "content://com.teslacoilsw.notifier/unread_count"), cv);

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

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);


                Intent notificationIntent = new Intent(c, Inbox.class);
                notificationIntent.putExtra(Inbox.EXTRA_UNREAD, true);
                PendingIntent intent = PendingIntent.getActivity(c, 0, notificationIntent, 0);

                //Intent for mark as read notification action
                PendingIntent readPI = MarkAsReadService.getMarkAsReadIntent(2, c, messageNames);

                {
                    int amount = messages.size();

                    NotificationCompat.InboxStyle notiStyle = new NotificationCompat.InboxStyle();
                    notiStyle.setBigContentTitle(
                            res.getQuantityString(R.plurals.mail_notification_title, amount,
                                    amount));
                    notiStyle.setSummaryText("");
                    for (Message m : messages) {
                        if (m.getAuthor() != null) {
                            notiStyle.addLine(c.getString(R.string.mail_notification_msg_from,
                                    m.getAuthor()));
                        } else {
                            notiStyle.addLine(c.getString(R.string.mail_notification_msg_via,
                                    m.getSubreddit()));

                        }
                    }

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(c, Reddit.CHANNEL_MAIL).setContentIntent(
                                    intent)
                                    .setSmallIcon(R.drawable.notif)
                                    .setTicker(
                                            res.getQuantityString(R.plurals.mail_notification_title,
                                                    amount, amount))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setContentTitle(
                                            res.getQuantityString(R.plurals.mail_notification_title,
                                                    amount, amount))
                                    .setStyle(notiStyle)
                                    .setGroup("MESSAGES")
                                    .setGroupSummary(true)
                                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)

                                    .addAction(R.drawable.ic_check_all_black,
                                            c.getString(R.string.mail_mark_read), readPI);
                    if (!SettingValues.notifSound) {
                        builder.setSound(null);
                    }
                    Notification notification = builder.build();

                    notificationManager.notify(0, notification);
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    for (Message m : messages) {
                        NotificationCompat.BigTextStyle notiStyle =
                                new NotificationCompat.BigTextStyle();
                        String contentTitle;
                        if (m.getAuthor() != null) {
                            notiStyle.setBigContentTitle(
                                    c.getString(R.string.mail_notification_msg_from,
                                            m.getAuthor()));
                            contentTitle =
                                    c.getString(R.string.mail_notification_author, m.getSubject(),
                                            m.getAuthor());
                        } else {
                            notiStyle.setBigContentTitle(
                                    c.getString(R.string.mail_notification_msg_via,
                                            m.getSubreddit()));
                            contentTitle = c.getString(R.string.mail_notification_subreddit,
                                    m.getSubject(), m.getSubreddit());
                        }
                        Intent openPIBase;
                        if (m.isComment()) {
                            openPIBase = new Intent(c, OpenContent.class);
                            String context = m.getDataNode().get("context").asText();
                            openPIBase.putExtra(OpenContent.EXTRA_URL,
                                    "https://reddit.com" + context.substring(0,
                                            context.lastIndexOf("/")));
                            openPIBase.setAction(m.getSubject());

                        } else {
                            openPIBase = new Intent(c, Inbox.class);
                            openPIBase.putExtra(Inbox.EXTRA_UNREAD, true);
                        }
                      //  openPIBase.setFlags(
                        //        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        PendingIntent openPi =
                                PendingIntent.getActivity(c, 3 + (int) m.getCreated().getTime(),
                                        openPIBase, 0);
                        notiStyle.bigText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(
                                m.getDataNode().get("body_html").asText())));

                        PendingIntent readPISingle = MarkAsReadService.getMarkAsReadIntent(
                                2 + (int) m.getCreated().getTime(), c,
                                new String[]{m.getFullName()});

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(c,
                                Reddit.CHANNEL_MAIL).setContentIntent(openPi)
                                        .setSmallIcon(R.drawable.notif)
                                        .setTicker(res.getQuantityString(
                                                R.plurals.mail_notification_title, 1, 1))
                                        .setWhen(System.currentTimeMillis())
                                        .setAutoCancel(true)
                                        .setContentTitle(contentTitle)
                                        .setContentText(Html.fromHtml(
                                                StringEscapeUtils.unescapeHtml4(
                                                        m.getDataNode().get("body_html").asText())))
                                        .setStyle(notiStyle)
                                        .setGroup("MESSAGES")
                                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                                        .addAction(R.drawable.ic_check_all_black,
                                                c.getString(R.string.mail_mark_read), readPISingle);
                        if (!SettingValues.notifSound) {
                            builder.setSound(null);
                        }
                        Notification notification = builder.build();
                        notificationManager.notify((int) m.getCreated().getTime(), notification);
                    }
                }
            }
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
                Collections.reverse(messages);
                NotificationManager notificationManager =
                        (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

                Intent notificationIntent = new Intent(c, ModQueue.class);

              //  notificationIntent.setFlags(
              //          Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent intent = PendingIntent.getActivity(c, 0, notificationIntent, 0);

                {
                    int amount = messages.size();

                    NotificationCompat.InboxStyle notiStyle = new NotificationCompat.InboxStyle();
                    notiStyle.setBigContentTitle(
                            res.getQuantityString(R.plurals.mod_mail_notification_title, amount,
                                    amount));
                    notiStyle.setSummaryText("");
                    for (Message m : messages) {
                        notiStyle.addLine(
                                c.getString(R.string.mod_mail_notification_msg, m.getAuthor()));
                    }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(c,
                            Reddit.CHANNEL_MODMAIL).setContentIntent(intent)
                                    .setSmallIcon(R.drawable.mod_png)
                                    .setTicker(res.getQuantityString(
                                            R.plurals.mod_mail_notification_title, amount, amount))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setGroupSummary(true)
                                    .setGroup("MODMAIL")
                                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                                    .setContentTitle(res.getQuantityString(
                                            R.plurals.mod_mail_notification_title, amount, amount))
                            .setStyle(notiStyle);
                    if (!SettingValues.notifSound) {
                        builder.setSound(null);
                    }
                    Notification notification = builder.build();

                    notificationManager.notify(1, notification);
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    for (Message m : messages) {
                        NotificationCompat.BigTextStyle notiStyle =
                                new NotificationCompat.BigTextStyle();
                        notiStyle.setBigContentTitle(
                                c.getString(R.string.mod_mail_notification_msg, m.getAuthor()));
                        notiStyle.bigText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(
                                m.getDataNode().get("body_html").asText())));

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(c,
                                Reddit.CHANNEL_MODMAIL).setContentIntent(intent)
                                        .setSmallIcon(R.drawable.mod_png)
                                        .setTicker(res.getQuantityString(
                                                R.plurals.mod_mail_notification_title, 1, 1))
                                        .setWhen(System.currentTimeMillis())
                                        .setAutoCancel(true)
                                        .setGroup("MODMAIL")
                                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                                        .setContentTitle(
                                                c.getString(R.string.mail_notification_author,
                                                        m.getSubject(), m.getAuthor()))
                                        .setContentText(Html.fromHtml(m.getBody()))
                                .setStyle(notiStyle);
                        if (!SettingValues.notifSound) {
                            builder.setSound(null);
                        }
                        Notification notification = builder.build();
                        notificationManager.notify((int) m.getCreated().getTime(), notification);
                    }
                }

            }
        }

        @Override
        protected List<Message> doInBackground(Void... params) {
            try {
                if (Authentication.isLoggedIn && Authentication.didOnline) {
                    InboxPaginator unread =
                            new InboxPaginator(Authentication.reddit, "moderator/unread");

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

    public static class AsyncGetSubs extends AsyncTask<Void, Void, List<Submission>> {

        public Context c;

        public AsyncGetSubs(Context context) {
            this.c = context;
        }

        @Override
        public void onPostExecute(List<Submission> messages) {
            if (messages != null) {
                if (!messages.isEmpty()) {
                    NotificationManager notificationManager =
                            (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                    for (Submission s : messages) {
                        Intent readIntent = new Intent(c, OpenContent.class);
                        readIntent.putExtra(OpenContent.EXTRA_URL,
                                "https://reddit.com" + s.getPermalink());
                        readIntent.setAction(s.getTitle());
                        PendingIntent readPI = PendingIntent.getActivity(c,
                                (int) (s.getCreated().getTime() / 1000), readIntent,
                                0);

                        Intent cancelIntent = new Intent(c, CancelSubNotifs.class);
                        cancelIntent.putExtra(CancelSubNotifs.EXTRA_SUB, s.getSubredditName());
                        PendingIntent cancelPi = PendingIntent.getActivity(c,  (int)s.getCreated().getTime() / 1000, cancelIntent,
                                0);


                        NotificationCompat.BigTextStyle notiStyle =
                                new NotificationCompat.BigTextStyle();
                        notiStyle.setBigContentTitle("/r/" + s.getSubredditName());

                        notiStyle.bigText(Html.fromHtml(s.getTitle() + " " + c.getString(
                                R.string.submission_properties_seperator_comments)) + " "
                                + s.getAuthor());


                        Notification notification =
                                new NotificationCompat.Builder(c, Reddit.CHANNEL_SUBCHECKING).setContentIntent(readPI)
                                        .setSmallIcon(R.drawable.notif)
                                        .setTicker(c.getString(
                                                R.string.sub_post_notifs_notification_title,
                                                s.getSubredditName()))
                                        .setWhen(System.currentTimeMillis())
                                        .setAutoCancel(true)
                                        .setContentTitle("/r/"
                                                + s.getSubredditName()
                                                + " " + c.getString(
                                                R.string.submission_properties_seperator_comments) + " "
                                                + Html.fromHtml(s.getTitle()))
                                        .setContentText(Html.fromHtml(s.getTitle()) + " " + c.getString(
                                                R.string.submission_properties_seperator_comments) + " "
                                                + s.getAuthor())
                                        .setColor(Palette.getColor(s.getSubredditName()))
                                        .setStyle(notiStyle)
                                        .addAction(R.drawable.close, c.getString(
                                                R.string.sub_post_notifs_notification_btn,
                                                s.getSubredditName()), cancelPi)
                                        .build();
                        notificationManager.notify((int) (s.getCreated().getTime() / 1000),
                                notification);
                    }
                }
            }
            if (Reddit.notificationTime != -1) new NotificationJobScheduler(c).start(c);
        }

        HashMap<String, Integer> subThresholds;

        @Override
        protected List<Submission> doInBackground(Void... params) {
            try {
                long lastTime = (System.currentTimeMillis() - (60000 * Reddit.notificationTime));
                ArrayList<Submission> toReturn = new ArrayList<>();
                ArrayList<String> rawSubs =
                        Reddit.stringToArray(Reddit.appRestart.getString(SUBS_TO_GET, ""));
                subThresholds = new HashMap<>();
                for (String s : rawSubs) {
                    try {
                        String[] split = s.split(":");
                        subThresholds.put(split[0].toLowerCase(Locale.ENGLISH),
                                Integer.valueOf(split[1]));
                    } catch (Exception ignored) {

                    }
                }
                if (subThresholds.isEmpty()) {
                    return null;
                }

                String first = "";
                boolean skipFirst = false;
                ArrayList<String> finalSubs = new ArrayList<>();
                for (String s : subThresholds.keySet()) {
                    if (!s.isEmpty() && !skipFirst) {
                        finalSubs.add(s);
                    } else {
                        skipFirst = true;
                        first = s;
                    }
                }
                SubredditPaginator unread = new SubredditPaginator(Authentication.reddit, first,
                        finalSubs.toArray(new String[0]));
                unread.setSorting(Sorting.NEW);
                unread.setLimit(30);
                if (unread.hasNext()) {
                    for (Submission subm : unread.next()) {
                        if (subm.getCreated().getTime() > lastTime && subm.getScore() >= subThresholds.get(subm.getSubredditName().toLowerCase()) && !HasSeen
                                .getSeen(subm)) {
                            toReturn.add(subm);
                        }
                    }
                }
                return toReturn;

            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            return null;
        }
    }

}