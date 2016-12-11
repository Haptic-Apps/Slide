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
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.Html;

import net.dean.jraw.models.Message;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.InboxPaginator;
import net.dean.jraw.paginators.SubmissionSearchPaginator;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.ccrama.redditslide.Activities.CancelSubNotifs;
import me.ccrama.redditslide.Activities.CommentsScreenSingle;
import me.ccrama.redditslide.Activities.Inbox;
import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Activities.OpenContent;
import me.ccrama.redditslide.Adapters.MarkAsReadService;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;

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
        new AsyncGetMail().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                if (Reddit.isPackageInstalled(c, "com.teslacoilsw.notifier")) {
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

                notificationIntent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

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

                    Notification notification =
                            new NotificationCompat.Builder(c).setContentIntent(intent)
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
                                    .addAction(R.drawable.ic_check_all_black,
                                            c.getString(R.string.mail_mark_read), readPI)
                                    .build();
                    if (SettingValues.notifSound) {
                        notification.defaults |= Notification.DEFAULT_SOUND;
                        notification.defaults |= Notification.DEFAULT_VIBRATE;
                    }

                    notificationManager.notify(0, notification);
                }

                for (Message m : messages) {
                    NotificationCompat.BigTextStyle notiStyle =
                            new NotificationCompat.BigTextStyle();
                    String contentTitle;
                    if (m.getAuthor() != null) {
                        notiStyle.setBigContentTitle(
                                c.getString(R.string.mail_notification_msg_from, m.getAuthor()));
                        contentTitle =
                                c.getString(R.string.mail_notification_author, m.getSubject(),
                                        m.getAuthor());
                    } else {
                        notiStyle.setBigContentTitle(
                                c.getString(R.string.mail_notification_msg_via, m.getSubreddit()));
                        contentTitle =
                                c.getString(R.string.mail_notification_subreddit, m.getSubject(),
                                        m.getSubreddit());
                    }
                    Intent openPIBase;
                    if (m.isComment()) {
                        openPIBase = new Intent(c, OpenContent.class);
                        String context = m.getDataNode().get("context").asText();
                        openPIBase.putExtra(OpenContent.EXTRA_URL, "https://reddit.com" + context.substring(0,
                                context.lastIndexOf("/")));
                    } else {
                        openPIBase = new Intent(c, Inbox.class);
                        openPIBase.putExtra(Inbox.EXTRA_UNREAD, true);
                    }
                    openPIBase.setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent openPi =
                            PendingIntent.getActivity(c, 3 + (int) m.getCreated().getTime(),
                                    openPIBase, 0);
                    notiStyle.bigText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(
                            m.getDataNode().get("body_html").asText())));

                    PendingIntent readPISingle = MarkAsReadService.getMarkAsReadIntent(2 + (int) m.getCreated().getTime(), c,  new String[]{m.getFullName()});

                    Notification notification =
                            new NotificationCompat.Builder(c).setContentIntent(openPi)
                                    .setSmallIcon(R.drawable.notif)
                                    .setTicker(
                                            res.getQuantityString(R.plurals.mail_notification_title,
                                                    1, 1))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setContentTitle(contentTitle)
                                    .setContentText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(
                                            m.getDataNode().get("body_html").asText())))
                                    .setStyle(notiStyle)
                                    .setGroup("MESSAGES")
                                    .addAction(R.drawable.ic_check_all_black,
                                            c.getString(R.string.mail_mark_read), readPISingle)
                                    .build();
                    if (SettingValues.notifSound) {
                        notification.defaults |= Notification.DEFAULT_SOUND;
                        notification.defaults |= Notification.DEFAULT_VIBRATE;
                    }
                    notificationManager.notify((int) m.getCreated().getTime(), notification);
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

                notificationIntent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

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

                    Notification notification =
                            new NotificationCompat.Builder(c).setContentIntent(intent)
                                    .setSmallIcon(R.drawable.mod)
                                    .setTicker(res.getQuantityString(
                                            R.plurals.mod_mail_notification_title, amount, amount))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setGroupSummary(true)
                                    .setGroup("MODMAIL")
                                    .setContentTitle(res.getQuantityString(
                                            R.plurals.mod_mail_notification_title, amount, amount))
                                    .setStyle(notiStyle)
                                    .build();
                    if (SettingValues.notifSound) {
                        notification.defaults |= Notification.DEFAULT_SOUND;
                        notification.defaults |= Notification.DEFAULT_VIBRATE;
                    }

                    notificationManager.notify(1, notification);
                }

                for (Message m : messages) {

                    NotificationCompat.BigTextStyle notiStyle =
                            new NotificationCompat.BigTextStyle();
                    notiStyle.setBigContentTitle(
                            c.getString(R.string.mod_mail_notification_msg, m.getAuthor()));
                    notiStyle.bigText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(
                            m.getDataNode().get("body_html").asText())));

                    Notification notification =
                            new NotificationCompat.Builder(c).setContentIntent(intent)
                                    .setSmallIcon(R.drawable.mod)
                                    .setTicker(res.getQuantityString(
                                            R.plurals.mod_mail_notification_title, 1, 1))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setGroup("MODMAIL")
                                    .setContentTitle(c.getString(R.string.mail_notification_author,
                                            m.getSubject(), m.getAuthor()))
                                    .setContentText(Html.fromHtml(m.getBody()))
                                    .setStyle(notiStyle)
                                    .build();
                    if (SettingValues.notifSound) {
                        notification.defaults |= Notification.DEFAULT_SOUND;
                        notification.defaults |= Notification.DEFAULT_VIBRATE;
                    }

                    notificationManager.notify((int) m.getCreated().getTime(), notification);
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
                        PendingIntent readPI = PendingIntent.getActivity(c,
                                (int) (s.getCreated().getTime() / 1000), readIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);


                        Intent cancelIntent = new Intent(c, CancelSubNotifs.class);
                        cancelIntent.putExtra(CancelSubNotifs.EXTRA_SUB, s.getSubredditName());
                        PendingIntent cancelPi = PendingIntent.getActivity(c, 2, cancelIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);


                        NotificationCompat.BigTextStyle notiStyle =
                                new NotificationCompat.BigTextStyle();
                        notiStyle.setBigContentTitle("/r/" + s.getSubredditName());

                        notiStyle.bigText(Html.fromHtml(s.getTitle() + c.getString(
                                R.string.submission_properties_seperator_comments))
                                + s.getAuthor());


                        Notification notification =
                                new NotificationCompat.Builder(c).setContentIntent(readPI)
                                        .setSmallIcon(R.drawable.notif)
                                        .setTicker(c.getString(
                                                R.string.sub_post_notifs_notification_title,
                                                s.getSubredditName()))
                                        .setWhen(System.currentTimeMillis())
                                        .setAutoCancel(true)
                                        .setContentTitle("/r/"
                                                + s.getSubredditName()
                                                + c.getString(
                                                R.string.submission_properties_seperator_comments)
                                                + Html.fromHtml(s.getTitle()))
                                        .setContentText(Html.fromHtml(s.getTitle() + c.getString(
                                                R.string.submission_properties_seperator_comments))
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
                int offsetSeconds = 28800; //8 hours in seconds
                ArrayList<Submission> toReturn = new ArrayList<>();
                ArrayList<String> rawSubs =
                        Reddit.stringToArray(Reddit.appRestart.getString(SUBS_TO_GET, ""));
                subThresholds = new HashMap<>();
                for (String s : rawSubs) {
                    try {
                        String[] split = s.split(":");
                        subThresholds.put(split[0].toLowerCase(), Integer.valueOf(split[1]));
                    } catch (Exception ignored) {

                    }
                }
                if (subThresholds.isEmpty()) {
                    return null;
                }

                String first = "";
                int count = 0, totalCount = 0;
                for (String s : subThresholds.keySet()) {
                    first = first + s + "+";
                    count++;
                    totalCount++;
                    if (count == 3 || totalCount == subThresholds.keySet().size()) {
                        first = first.substring(0, first.length() - 1);
                        SubmissionSearchPaginator unread =
                                new SubmissionSearchPaginator(Authentication.reddit,
                                        "timestamp:" + ((lastTime / 1000) + offsetSeconds)
                                                //Go an hour back just in case
                                                + ".." + ((System.currentTimeMillis() / 1000)
                                                + offsetSeconds));
                        LogUtil.v("/r/" + first + "/search?q=timestamp:" + ((lastTime / 1000)
                                + offsetSeconds) + ".." + ((System.currentTimeMillis() / 1000)
                                + offsetSeconds));
                        unread.setSearchSorting(SubmissionSearchPaginator.SearchSort.NEW);
                        unread.setSyntax(SubmissionSearchPaginator.SearchSyntax.CLOUDSEARCH);
                        unread.setSubreddit(first);
                        unread.setLimit(30);
                        if (unread.hasNext()) {
                            for (Submission subm : unread.next()) {
                                if (subm.getScore() >= subThresholds.get(
                                        subm.getSubredditName().toLowerCase())
                                        && !HasSeen.getSeen(subm)
                                        && subm.getDataNode().get("created").asLong()
                                        + offsetSeconds >= lastTime / 1000) {
                                    toReturn.add(subm);
                                }
                            }
                        }
                        first = "";
                        count = 0;
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