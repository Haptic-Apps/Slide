package me.ccrama.redditslide.Widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.OpenContent;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by carlo_000 on 5/4/2016.
 */
public class ListViewWidgetService extends RemoteViewsService {
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListViewRemoteViewsFactory(this.getApplicationContext(), intent, "android", intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0));
    }
}

class ListViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private ArrayList<Submission> records;
    String subreddit;
    int id;

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
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SubredditPaginator p = new SubredditPaginator(Authentication.reddit, SubredditWidgetProvider.getSubFromId(id, mContext));
                p.setLimit(50);
                records = new ArrayList<>(p.next());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Intent widgetUpdateIntent = new Intent(mContext, SubredditWidgetProvider.class);
                widgetUpdateIntent.setAction(SubredditWidgetProvider.UPDATE_MEETING_ACTION);
                widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        id);
                mContext.sendBroadcast(widgetUpdateIntent);
            }
        }.execute();
    }

    // Given the position (index) of a WidgetItem in the array, use the item's text value in
    // combination with the app widget item XML file to construct a RemoteViews object.
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.
        // Construct a RemoteViews item based on the app widget item XML file, and set the
        // text based on the position.
        int view = R.layout.submission_widget;
        switch(SubredditWidgetProvider.getThemeFromId(id, mContext)){
            case 2:
                view = R.layout.submission_widget_light;
                break;
        }
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(), view);
        // feed row
        Submission data = records.get(position);

        rv.setTextViewText(R.id.title, Html.fromHtml(data.getTitle()));
        rv.setTextViewText(R.id.score, data.getScore() + "");
        rv.setTextViewText(R.id.comments, data.getCommentCount() + "");
        rv.setTextViewText(R.id.information, data.getAuthor() + " " + TimeUtils.getTimeAgo(data.getCreated().getTime(), mContext));
        rv.setTextViewText(R.id.subreddit, data.getSubredditName());
        rv.setTextColor(R.id.subreddit, Palette.getColor(data.getSubredditName()));
        if (data.getThumbnailType() == Submission.ThumbnailType.URL) {
            rv.setImageViewBitmap(R.id.thumbimage2, ((Reddit) mContext.getApplicationContext()).getImageLoader().loadImageSync(data.getThumbnail()));
            rv.setViewVisibility(R.id.thumbimage2, View.VISIBLE);
        } else
            rv.setViewVisibility(R.id.thumbimage2, View.GONE);

        Bundle infos = new Bundle();
        infos.putString(OpenContent.EXTRA_URL, data.getPermalink());
        infos.putBoolean("popup", true);
        final Intent activityIntent = new Intent();
        activityIntent.putExtras(infos);

        rv.setOnClickFillInIntent(R.id.card, activityIntent);

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
