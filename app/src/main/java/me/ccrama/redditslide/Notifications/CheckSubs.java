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

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.CancelSubNotifs;
import me.ccrama.redditslide.Activities.OpenContent;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

public class CheckSubs extends BroadcastReceiver {

    private Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        if (NetworkUtil.isConnected(c)) {
            new AsyncGetSubs().execute();
        }
    }


    private class AsyncGetSubs extends AsyncTask<Void, Void, List<Submission>> {

        @Override
        public void onPostExecute(List<Submission> messages) {
            Resources res = c.getResources();
            if (messages != null) {
                if (!messages.isEmpty()) {
                    NotificationManager notificationManager =
                            (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                    for (Submission s : messages) {
                        Intent readIntent = new Intent(c, OpenContent.class);
                        readIntent.putExtra(OpenContent.EXTRA_URL, "https://reddit.com" + s.getPermalink());
                        PendingIntent readPI = PendingIntent.getService(c, 2, readIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);


                        Intent cancelIntent = new Intent(c, CancelSubNotifs.class);
                        cancelIntent.putExtra(CancelSubNotifs.EXTRA_SUB, s.getSubredditName());
                        PendingIntent cancelPi = PendingIntent.getService(c, 2, cancelIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);


                        NotificationCompat.BigTextStyle notiStyle =
                                new NotificationCompat.BigTextStyle();
                        notiStyle.setBigContentTitle("/r/" + s.getSubredditName());

                        notiStyle.bigText(Html.fromHtml(s.getTitle() + c.getString(
                                R.string.submission_properties_seperator_comments)) + s.getAuthor());


                        Notification notification =
                                new NotificationCompat.Builder(c).setContentIntent(readPI)
                                        .setSmallIcon(R.drawable.notif)
                                        .setTicker("New post in /r/" + s.getSubredditName())
                                        .setWhen(System.currentTimeMillis())
                                        .setAutoCancel(true)
                                        .setContentTitle("/r/"
                                                + s.getSubredditName()
                                                + c.getString(
                                                R.string.submission_properties_seperator_comments)
                                                + s.getTitle())
                                        .setContentText(Html.fromHtml(s.getTitle() + c.getString(
                                                R.string.submission_properties_seperator_comments)) + s.getAuthor())
                                        .setColor(Palette.getColor(s.getSubredditName()))
                                        .setStyle(notiStyle)
                                        .addAction(R.drawable.close,
                                                "Stop tracking /r/" + s.getSubredditName(),
                                                cancelPi)
                                        .build();
                        notificationManager.notify((int) (s.getCreated().getTime() / 1000),
                                notification);
                    }
                } new NotificationJobScheduler(c).start(c);

            }
        }

        @Override
        protected List<Submission> doInBackground(Void... params) {
            try {
                long lastTime = (System.currentTimeMillis() - 600000);
                ArrayList<Submission> toReturn = new ArrayList<>();
                if (Authentication.isLoggedIn && Authentication.didOnline) {
                    ArrayList<String> subs =
                            Reddit.stringToArray(Reddit.appRestart.getString("subsToGet", ""));
                    if (subs.isEmpty()) {
                        return null;
                    }
                    for (String s : subs) {
                        if (!s.isEmpty()) {
                            LogUtil.v("Sub is " + s);
                            SubredditPaginator unread =
                                    new SubredditPaginator(Authentication.reddit, s);
                            unread.setSorting(Sorting.NEW);
                            unread.setTimePeriod(TimePeriod.HOUR);
                            unread.setLimit(5);
                            if (unread.hasNext()) {
                                for (Submission subm : unread.next()) {
                                    if (subm.getCreated().getTime() > lastTime) {
                                        toReturn.add(subm);
                                    }
                                }
                            }
                        }
                    }
                    return toReturn;
                }
            } catch (Exception ignored)

            {
                ignored.printStackTrace();

            }

            return null;
        }
    }

}