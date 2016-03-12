package me.ccrama.redditslide.Widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by carlo_000 on 3/11/2016.
 *
 * Heavily adapted from https://laaptu.wordpress.com/2013/07/24/populate-appwidget-listview-with-remote-datadata-from-web/
 */
public class FetchData extends Service {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public static ArrayList<Submission> listItemList;

    @Override
    public IBinder onBind(Intent arg0) {
        sub = arg0.getStringExtra("sub");
        return null;
    }

    String sub;


    /**
     * Retrieve appwidget id from intent it is needed to update widget later
     * initialize our AQuery class
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID))
            appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        //do
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                LogUtil.v("Loading data");
                if(Authentication.reddit != null){
                   SubredditPaginator paginator = new SubredditPaginator(Authentication.reddit, sub);
                    paginator.setLimit(25);
                    paginator.setSorting(Sorting.HOT);
                    paginator.setTimePeriod(TimePeriod.HOUR);
                    listItemList = new ArrayList<>(paginator.next());
                    return true;
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if(aBoolean){
                    LogUtil.v("Data loaded");

                    populateWidget();
                }
            }
        }.execute();
        return super.onStartCommand(intent, flags, startId);
    }



    /**
     * Method which sends broadcast to WidgetProvider
     * so that widget is notified to do necessary action
     * and here action == WidgetProvider.DATA_FETCHED
     */
    private void populateWidget() {

        Intent widgetUpdateIntent = new Intent();
        widgetUpdateIntent.setAction(PostWidgetProvider.DATA_FETCHED);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                appWidgetId);
        sendBroadcast(widgetUpdateIntent);

        this.stopSelf();
    }
}