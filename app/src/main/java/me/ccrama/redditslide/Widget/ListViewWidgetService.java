package me.ccrama.redditslide.Widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thumbnails;
import net.dean.jraw.paginators.DomainPaginator;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import me.ccrama.redditslide.Activities.OpenContent;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 5/4/2016.
 */
public class ListViewWidgetService extends RemoteViewsService {
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListViewRemoteViewsFactory(this.getApplicationContext(), intent, "android",
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0));
    }
}

class ListViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context               mContext;
    private ArrayList<Submission> records;
    String subreddit;
    int    id;

    public ListViewRemoteViewsFactory(Context context, Intent intent, String subreddit, int id) {
        mContext = context;
        this.subreddit = subreddit;
        this.id = id;
    }

    // Initialize the data set.
    public void onCreate() {
        // In onCreate() you set up any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        records = new ArrayList<>();
        if (NetworkUtil.isConnected(mContext)) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if (Authentication.reddit == null) {
                        new Authentication(mContext.getApplicationContext());
                        Authentication.me = Authentication.reddit.me();
                        Authentication.mod = Authentication.me.isMod();

                        Authentication.authentication.edit()
                                .putBoolean(Reddit.SHARED_PREF_IS_MOD, Authentication.mod)
                                .apply();

                        if (Reddit.notificationTime != -1) {
                            Reddit.notifications = new NotificationJobScheduler(mContext);
                            Reddit.notifications.start(mContext.getApplicationContext());
                        }

                        if (Reddit.cachedData.contains("toCache")) {
                            Reddit.autoCache = new AutoCacheScheduler(mContext);
                            Reddit.autoCache.start(mContext.getApplicationContext());
                        }

                        final String name = Authentication.me.getFullName();
                        Authentication.name = name;
                        LogUtil.v("AUTHENTICATED");

                        if (Authentication.reddit.isAuthenticated()) {
                            final Set<String> accounts =
                                    Authentication.authentication.getStringSet("accounts",
                                            new HashSet<String>());
                            if (accounts.contains(name)) { //convert to new system
                                accounts.remove(name);
                                accounts.add(name + ":" + Authentication.refresh);
                                Authentication.authentication.edit()
                                        .putStringSet("accounts", accounts)
                                        .apply(); //force commit
                            }
                            Authentication.isLoggedIn = true;
                            Reddit.notFirst = true;
                        }
                    }
                    String sub = SubredditWidgetProvider.getSubFromId(id, mContext);
                    Paginator p;
                    if (sub.equals("frontpage")) {
                        p = new SubredditPaginator(Authentication.reddit);
                    } else if (!sub.contains(".")) {
                        p = new SubredditPaginator(Authentication.reddit, sub);
                    } else {
                        p = new DomainPaginator(Authentication.reddit, sub);
                    }
                    p.setLimit(50);
                    switch (SubredditWidgetProvider.getSorting(id, mContext)) {
                        case 0:
                            p.setSorting(Sorting.HOT);
                            break;
                        case 1:
                            p.setSorting(Sorting.NEW);
                            break;
                        case 2:
                            p.setSorting(Sorting.RISING);
                            break;
                        case 3:
                            p.setSorting(Sorting.TOP);
                            break;
                        case 4:
                            p.setSorting(Sorting.CONTROVERSIAL);
                            break;
                        case 5:
                            p.setSorting(Sorting.BEST);
                            break;
                    }
                    switch (SubredditWidgetProvider.getSortingTime(id, mContext)) {
                        case 0:
                            p.setTimePeriod(TimePeriod.HOUR);
                            break;
                        case 1:
                            p.setTimePeriod(TimePeriod.DAY);
                            break;
                        case 2:
                            p.setTimePeriod(TimePeriod.WEEK);
                            break;
                        case 3:
                            p.setTimePeriod(TimePeriod.MONTH);
                            break;
                        case 4:
                            p.setTimePeriod(TimePeriod.YEAR);
                            break;
                        case 5:
                            p.setTimePeriod(TimePeriod.ALL);
                            break;
                    }

                    try {
                        ArrayList<Submission> s = new ArrayList<>(p.next());
                        records = new ArrayList<>();
                        for (Submission subm : s) {
                            if (!PostMatch.doesMatch(subm) && !subm.isStickied()) {
                                records.add(subm);
                            }
                        }
                    } catch (Exception e) {

                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    Intent widgetUpdateIntent = new Intent(mContext, SubredditWidgetProvider.class);
                    widgetUpdateIntent.setAction(SubredditWidgetProvider.UPDATE_MEETING_ACTION);
                    widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
                    mContext.sendBroadcast(widgetUpdateIntent);
                }
            }.execute();
        } else {
            Intent widgetUpdateIntent = new Intent(mContext, SubredditWidgetProvider.class);
            widgetUpdateIntent.setAction(SubredditWidgetProvider.UPDATE_MEETING_ACTION);
            widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            mContext.sendBroadcast(widgetUpdateIntent);
        }
    }

    // Given the position (index) of a WidgetItem in the array, use the item's text value in
    // combination with the app widget item XML file to construct a RemoteViews object.
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.
        // Construct a RemoteViews item based on the app widget item XML file, and set the
        // text based on the position.
        int view = R.layout.submission_widget_light;
        switch (SubredditWidgetProvider.getViewType(id, mContext)) {
            case 1:
            case 0:
                if (SubredditWidgetProvider.getThemeFromId(id, mContext) == 2) {
                    view = R.layout.submission_widget_light;
                } else {
                    view = R.layout.submission_widget;
                }

                break;
            case 2:
                if (SubredditWidgetProvider.getThemeFromId(id, mContext) == 2) {
                    view = R.layout.submission_widget_compact_light;
                } else {
                    view = R.layout.submission_widget_compact;
                }
                break;
        }
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(), view);
        try {

            // feed row
            Submission data = records.get(position);

            rv.setTextViewText(R.id.title, Html.fromHtml(data.getTitle()));
            rv.setTextViewText(R.id.score, data.getScore() + "");
            rv.setTextViewText(R.id.comments, data.getCommentCount() + "");
            rv.setTextViewText(R.id.information,
                    data.getAuthor() + " " + TimeUtils.getTimeAgo(data.getCreated().getTime(),
                            mContext));
            rv.setTextViewText(R.id.subreddit, data.getSubredditName());
            rv.setTextColor(R.id.subreddit, Palette.getColor(data.getSubredditName()));
            if (SubredditWidgetProvider.getViewType(id, mContext) == 1) {
                Thumbnails s = data.getThumbnails();
                rv.setViewVisibility(R.id.thumbimage2, View.GONE);
                if (s != null && s.getVariations() != null && s.getSource() != null) {
                    rv.setImageViewBitmap(R.id.bigpic,
                            ((Reddit) mContext.getApplicationContext()).getImageLoader()
                                    .loadImageSync(
                                            Html.fromHtml(data.getThumbnails().getSource().getUrl())
                                                    .toString()));
                    rv.setViewVisibility(R.id.bigpic, View.VISIBLE);
                } else {
                    rv.setViewVisibility(R.id.bigpic, View.GONE);
                }
            } else {
                if (SubredditWidgetProvider.getViewType(id, mContext) != 2) {
                    rv.setViewVisibility(R.id.bigpic, View.GONE);
                }
                if (data.getThumbnailType() == Submission.ThumbnailType.URL) {
                    rv.setImageViewBitmap(R.id.thumbimage2,
                            ((Reddit) mContext.getApplicationContext()).getImageLoader()
                                    .loadImageSync(data.getThumbnail()));
                    rv.setViewVisibility(R.id.thumbimage2, View.VISIBLE);
                } else {
                    rv.setViewVisibility(R.id.thumbimage2, View.GONE);
                }
            }
            switch (SubredditWidgetProvider.getViewType(id, mContext)) {
                case 1:
                case 0:
                case 2:
                    if (SubredditWidgetProvider.getThemeFromId(id, mContext) == 2) {
                    } else {
                        rv.setTextColor(R.id.title, Color.WHITE);
                        rv.setTextColor(R.id.score, Color.WHITE);
                        rv.setTextColor(R.id.comments, Color.WHITE);
                        rv.setTextColor(R.id.information, Color.WHITE);
                    }

                    break;
            }

            Bundle infos = new Bundle();
            infos.putString(OpenContent.EXTRA_URL, data.getPermalink());
            infos.putBoolean("popup", true);
            final Intent activityIntent = new Intent();
            activityIntent.putExtras(infos);
            activityIntent.setAction(data.getTitle());
            rv.setOnClickFillInIntent(R.id.card, activityIntent);
        } catch (Exception e) {

        }
        return rv;
    }

    public int getCount() {
        return records.size();
    }


    public void onDataSetChanged() {

        // Fetching JSON data from server and add them to records arraylist
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public void onDestroy() {
        records.clear();
    }

    public boolean hasStableIds() {
        return true;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

}
