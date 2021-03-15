package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

import me.ccrama.redditslide.Adapters.SubChooseAdapter;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.ColorPreferences;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Widget.SubredditWidgetProvider;
import me.ccrama.redditslide.util.SortingUtil;

/**
 * Created by carlo_000 on 5/4/2016.
 */
public class SetupWidget extends BaseActivity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        disableSwipeBackLayout();
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
    View header;
    public void doShortcut() {

        setContentView(R.layout.activity_setup_widget);
        setupAppBar(R.id.toolbar, R.string.widget_creation_title, true, true);
        header = getLayoutInflater().inflate(R.layout.widget_header, null);

        ListView list = (ListView)findViewById(R.id.subs);
        final ArrayList<String> sorted = UserSubscriptions.getSubscriptionsForShortcut(SetupWidget.this);
        final SubChooseAdapter adapter = new SubChooseAdapter(this, sorted, UserSubscriptions.getAllSubreddits(this));

        list.addHeaderView(header);
        list.setAdapter(adapter);

        (header.findViewById(R.id.sort)).clearFocus();
        ((EditText)header.findViewById(R.id.sort)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String result = editable.toString();
                adapter.getFilter().filter(result);
            }
        });
    }

    public String name;


    /**
     * This method right now displays the widget and starts a Service to fetch
     * remote data from Server
     */
    public void startWidget() {
        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                SubredditWidgetProvider.setSubFromid(appWidgetId, name, SetupWidget.this);
                int theme = 0;
                switch(((RadioGroup)header.findViewById(R.id.theme)).getCheckedRadioButtonId()){
                    case R.id.dark:
                        theme = 1;
                        break;
                    case R.id.light:
                        theme = 2;
                        break;
                }
                int view = 0;
                switch(((RadioGroup)header.findViewById(R.id.type)).getCheckedRadioButtonId()){
                    case R.id.big:
                        view = 1;
                        break;
                    case R.id.compact:
                        view = 2;
                        break;
                }

                SubredditWidgetProvider.setThemeToId(appWidgetId, theme, SetupWidget.this);
                SubredditWidgetProvider.setViewType(appWidgetId, view, SetupWidget.this);
                SubredditWidgetProvider.setSorting(appWidgetId, i, SetupWidget.this);
                if (i == 3 || i == 4) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SetupWidget.this);
                    builder.setTitle(R.string.sorting_choose);
                    builder.setSingleChoiceItems(SortingUtil.getSortingTimesStrings(),
                            SortingUtil.getSortingTimeId(""),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SubredditWidgetProvider.setSortingTime(appWidgetId, i, SetupWidget.this);

                                    {
                                        Intent intent = new Intent();
                                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                                        setResult(Activity.RESULT_OK, intent);
                                    }

                                    Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                                            SetupWidget.this, SubredditWidgetProvider.class);
                                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
                                    sendBroadcast(intent);

                                    finish();
                                }
                            });
                    builder.show();
                } else {
                    {
                        Intent intent = new Intent();
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                        setResult(Activity.RESULT_OK, intent);
                    }

                    Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                            SetupWidget.this, SubredditWidgetProvider.class);
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
                    sendBroadcast(intent);

                    finish();
                }

            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(SetupWidget.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(SortingUtil.getSortingStrings(), SortingUtil.getSortingId(""),
                l2);
        builder.show();
        // this intent is essential to show the widget
        // if this intent is not included,you can't show
        // widget on homescreen

    }

}
