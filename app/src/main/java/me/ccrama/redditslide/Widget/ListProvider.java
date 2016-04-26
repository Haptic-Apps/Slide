package me.ccrama.redditslide.Widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.TimeUtils;

/**
 * Created by carlo_000 on 3/11/2016.
 */
public class ListProvider implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<Submission> listItemList = new ArrayList<>();
    private Context context = null;

    public ListProvider(Context context, Intent intent) {
        this.context = context;
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem();
    }

    private void populateListItem() {
        if (FetchData.listItemList != null)
            listItemList = (ArrayList<Submission>) FetchData.listItemList.clone();
        else
            listItemList = new ArrayList<>();

    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.widget_submission_list);
        Submission listItem = listItemList.get(position);
        remoteView.setTextViewText(R.id.title, listItem.getTitle());
        remoteView.setTextViewText(R.id.information, TimeUtils.getTimeAgo(listItem.getCreated().getTime(), context) + " /r/" + listItem.getSubredditName());
        return remoteView;
    }


    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

}