package me.ccrama.redditslide;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by ccrama on 10/2/2015.
 */
public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
