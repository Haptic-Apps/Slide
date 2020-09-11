package me.ccrama.redditslide.Notifications;

/**
 * Created by carlo_000 on 10/13/2015.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.text.HtmlCompat;

import net.dean.jraw.models.Message;
import net.dean.jraw.paginators.InboxPaginator;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.Inbox;
import me.ccrama.redditslide.Activities.OpenContent;
import me.ccrama.redditslide.Adapters.MarkAsReadService;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

public class CheckForMailSingle extends BroadcastReceiver {

    public static final String SUBS_TO_GET = "SUBREDDIT_NOTIFS";
    private Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        if (Authentication.reddit == null || !Authentication.reddit.isAuthenticated()) {
            Reddit.authentication = new Authentication(context);
        }
        new AsyncGetMailSingle().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class AsyncGetMailSingle extends AsyncTask<Void, Void, List<Message>> {

        @Override
        public void onPostExecute(List<Message> messages) {
            Resources res = c.getResources();
            if (messages != null && !messages.isEmpty()) {
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
                Message message = messages.get(0);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);


                Intent notificationIntent = new Intent(c, Inbox.class);
                notificationIntent.putExtra(Inbox.EXTRA_UNREAD, true);

                notificationIntent.setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent intent = PendingIntent.getActivity(c, 0, notificationIntent, 0);

                //Intent for mark as read notification action
                PendingIntent readPI = MarkAsReadService.getMarkAsReadIntent(2, c,
                        new String[]{message.getFullName()});

                {

                    NotificationCompat.InboxStyle notiStyle = new NotificationCompat.InboxStyle();
                    notiStyle.setBigContentTitle(
                            res.getQuantityString(R.plurals.mail_notification_title, 1, 1));
                    notiStyle.setSummaryText("");
                    if (message.getAuthor() != null) {
                        notiStyle.addLine(c.getString(R.string.mail_notification_msg_from,
                                message.getAuthor()));
                    } else {
                        notiStyle.addLine(c.getString(R.string.mail_notification_msg_via,
                                message.getSubreddit()));
                    }

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(c, Reddit.CHANNEL_MAIL).setContentIntent(
                                    intent)
                                    .setSmallIcon(R.drawable.notif)
                                    .setTicker(
                                            res.getQuantityString(R.plurals.mail_notification_title,
                                                    1, 1))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setContentTitle(
                                            res.getQuantityString(R.plurals.mail_notification_title,
                                                    1, 1))
                                    .setStyle(notiStyle)
                                    .setGroup("MESSAGES")
                                    .setGroupSummary(true)
                                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                                    .addAction(R.drawable.check_all,
                                            c.getString(R.string.mail_mark_read), readPI);
                    if (!SettingValues.notifSound) {
                        builder.setSound(null);
                    }
                    Notification notification = builder.build();

                    notificationManager.notify(0, notification);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    NotificationCompat.BigTextStyle notiStyle =
                            new NotificationCompat.BigTextStyle();
                    String contentTitle;
                    if (message.getAuthor() != null) {
                        notiStyle.setBigContentTitle(
                                c.getString(R.string.mail_notification_msg_from,
                                        message.getAuthor()));
                        contentTitle =
                                c.getString(R.string.mail_notification_author, message.getSubject(),
                                        message.getAuthor());
                    } else {
                        notiStyle.setBigContentTitle(c.getString(R.string.mail_notification_msg_via,
                                message.getSubreddit()));
                        contentTitle = c.getString(R.string.mail_notification_subreddit,
                                message.getSubject(), message.getSubreddit());
                    }
                    Intent openPIBase;
                    if (message.isComment()) {
                        openPIBase = new Intent(c, OpenContent.class);
                        String context = message.getDataNode().get("context").asText();
                        openPIBase.putExtra(OpenContent.EXTRA_URL,
                                "https://reddit.com" + context.substring(0,
                                        context.lastIndexOf("/")));
                        openPIBase.setAction(message.getSubject());
                    } else {
                        openPIBase = new Intent(c, Inbox.class);
                        openPIBase.putExtra(Inbox.EXTRA_UNREAD, true);
                    }
                    openPIBase.setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent openPi =
                            PendingIntent.getActivity(c, 3 + (int) message.getCreated().getTime(),
                                    openPIBase, 0);

                    String unescape = StringEscapeUtils.unescapeHtml4(message.getDataNode().get("body_html").asText());
                    notiStyle.bigText(HtmlCompat.fromHtml(unescape, HtmlCompat.FROM_HTML_MODE_LEGACY));

                    PendingIntent readPISingle = MarkAsReadService.getMarkAsReadIntent(
                            2 + (int) message.getCreated().getTime(), c,
                            new String[]{message.getFullName()});

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(c, Reddit.CHANNEL_MAIL).setContentIntent(
                                    openPi)
                                    .setSmallIcon(R.drawable.notif)
                                    .setTicker(
                                            res.getQuantityString(R.plurals.mail_notification_title,
                                                    1, 1))
                                    .setWhen(System.currentTimeMillis())
                                    .setAutoCancel(true)
                                    .setContentTitle(contentTitle)
                                    .setContentText(HtmlCompat.fromHtml(unescape, HtmlCompat.FROM_HTML_MODE_LEGACY))
                                    .setStyle(notiStyle)
                                    .setGroup("MESSAGES")
                                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                                    .addAction(R.drawable.check_all,
                                            c.getString(R.string.mail_mark_read), readPISingle);
                    if (!SettingValues.notifSound) {
                        builder.setSound(null);
                    }
                    Notification notification = builder.build();
                    notificationManager.notify((int) message.getCreated().getTime(), notification);
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
}
