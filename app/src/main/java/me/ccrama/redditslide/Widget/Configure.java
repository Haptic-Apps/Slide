package me.ccrama.redditslide.Widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.OpenContent;
import me.ccrama.redditslide.Adapters.SubredditListingAdapter;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;

/**
 * Created by ccrama on 10/2/2015.
 */
public class Configure extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        doShortcut();
        assignAppWidgetId();


    }

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private void assignAppWidgetId() {
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void doShortcut() {


        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        final Intent shortcutIntent = new Intent(Configure.this, OpenContent.class);

                        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Configure.this);

                        builder.setTitle(R.string.subreddit_chooser);
                        final ArrayList<String> sorted = UserSubscriptions.sort(UserSubscriptions.getSubscriptions(Configure.this));
                        builder.setAdapter(new SubredditListingAdapter(Configure.this, sorted), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sub = sorted.get(which);
                                startWidget();
                                finish();
                            }
                        });

                        builder.create().show();
                    }
                }
        );

    }

    public String sub;

    private void startWidget() {

        // this intent is essential to show the widget
        // if this intent is not included,you can't show
        // widget on homescreen
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("sub", sub);
        setResult(Activity.RESULT_OK, intent);

        // start your service
        // to fetch data from web
        Intent serviceIntent = new Intent(this, FetchData.class);
        serviceIntent
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra("sub", sub);
        startService(serviceIntent);

        // finish this activity
        this.finish();

    }

}
