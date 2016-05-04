package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.ArrayList;

import me.ccrama.redditslide.Adapters.SubredditListingAdapter;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Widget.ListViewWidgetService;
import me.ccrama.redditslide.Widget.SubredditWidgetProvider;

/**
 * Created by carlo_000 on 5/4/2016.
 */
public class SetupWidget extends Activity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTheme().applyStyle(new FontPreferences(this).getCommentFontStyle().getResId(), true);
        getTheme().applyStyle(new FontPreferences(this).getPostFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        super.onCreate(savedInstanceState);
        assignAppWidgetId();
        doShortcut();
    }

    /**
     * Widget configuration activity,always receives appwidget Id appWidget Id =
     * unique id that identifies your widget analogy : same as setting view id
     * via @+id/viewname on layout but appwidget id is assigned by the system
     * itself
     */
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

                        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SetupWidget.this);

                        builder.setTitle(R.string.subreddit_chooser);
                        final ArrayList<String> sorted = UserSubscriptions.getAllSubreddits(SetupWidget.this);
                        builder.setAdapter(new SubredditListingAdapter(SetupWidget.this, sorted), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                name = sorted.get(which);
                                startWidget();
                            }
                        });

                        builder.create().show();
                    }
                }
        );

    }

    String name;


    /**
     * This method right now displays the widget and starts a Service to fetch
     * remote data from Server
     */
    private void startWidget() {

        // this intent is essential to show the widget
        // if this intent is not included,you can't show
        // widget on homescreen
        SubredditWidgetProvider.setSubFromid(appWidgetId, name, this);

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(Activity.RESULT_OK, intent);

        // start your service
        // to fetch data from web
        Intent serviceIntent = new Intent(this, ListViewWidgetService.class);
        serviceIntent
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startService(serviceIntent);

        // finish this activity
        this.finish();

    }

}