package me.ccrama.redditslide.Widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import me.ccrama.redditslide.R;

/**
 * Created by carlo_000 on 3/11/2016.
 */
public class PostWidgetProvider extends AppWidgetProvider {

    // String to be sent on Broadcast as soon as Data is Fetched
    // should be included on WidgetProvider manifest intent action
    // to be recognized by this WidgetProvider to receive broadcast

    public static final String DATA_FETCHED = "me.ccrama.redditslide.DATA_FETCHED";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            Intent serviceIntent = new Intent(context, FetchData.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetIds[i]);
            context.startService(serviceIntent);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private RemoteViews updateWidgetListView(Context context, int appWidgetId) {

        // which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.subreddit_widget);
        // RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, RemoteViewService.class);
        // passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // setting a unique Uri to the intent
        // don't know its purpose to me right now
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        // setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(appWidgetId, R.id.submissions,
                svcIntent);
        // setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.submissions, R.id.submissions); ///todo fix
        return remoteViews;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(DATA_FETCHED)) {
            int appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(context);

            RemoteViews remoteViews = updateWidgetListView(context, appWidgetId);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

    }
}
