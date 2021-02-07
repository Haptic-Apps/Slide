package me.ccrama.redditslide.Widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by carlo_000 on 5/4/2016.
 */
public class ListViewWidgetService extends RemoteViewsService {
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListViewRemoteViewsFactory(this.getApplicationContext(), intent, "android",
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0));
    }
}
