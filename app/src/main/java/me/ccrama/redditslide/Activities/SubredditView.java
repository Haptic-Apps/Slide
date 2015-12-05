package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.Adapters.SubmissionAdapter;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.SubredditStorageNoContext;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.ToastHelpCreation;
import me.ccrama.redditslide.Visuals.Palette;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

public class SubredditView extends BaseActivityAnim {


    private DrawerLayout drawerLayout;
    private RecyclerView rv;
    private String subreddit;
    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private SubmissionAdapter adapter;
    private SubredditPosts posts;

    private void restartTheme() {
        Intent intent = this.getIntent();
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 2) {
            // Make sure the request was successful
            adapter = new SubmissionAdapter(this, posts, rv, subreddit);
            rv.setAdapter(adapter);
        } else if (requestCode == 1) {
            restartTheme();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subreddit = getIntent().getExtras().getString("subreddit", "");
        applyColorTheme(subreddit);
        setContentView(R.layout.activity_singlesubreddit);
        setupSubredditAppBar(R.id.toolbar, subreddit, true, subreddit);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setResult(3);

        rv = ((RecyclerView) findViewById(R.id.vertical_content));
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE || !Reddit.tabletUI) {
            final PreCachingLayoutManager mLayoutManager;
            mLayoutManager = new PreCachingLayoutManager(this);
            rv.setLayoutManager(mLayoutManager);
        } else {
            final StaggeredGridLayoutManager mLayoutManager;
            mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
            rv.setLayoutManager(mLayoutManager);
        }
        rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                visibleItemCount = rv.getLayoutManager().getChildCount();
                totalItemCount = rv.getLayoutManager().getItemCount();
                if (rv.getLayoutManager() instanceof PreCachingLayoutManager) {
                    pastVisiblesItems = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                } else {
                    int[] firstVisibleItems = null;
                    firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                    if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                        pastVisiblesItems = firstVisibleItems[0];
                    }
                }

                if (!posts.loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        posts.loading = true;
                        posts.loadMore(adapter, false, subreddit);

                    }
                }
            }
        });
        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(subreddit, this));

        mSwipeRefreshLayout.setRefreshing(true);
        posts = new SubredditPosts(subreddit);
        adapter = new SubmissionAdapter(this, posts, rv, subreddit);
        rv.setAdapter(adapter);

        doSubSidebar(subreddit);
        try {
            posts.bindAdapter(adapter, mSwipeRefreshLayout);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //TODO catch errors
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        posts.loadMore(adapter, true, subreddit);

                        //TODO catch errors
                    }
                }
        );


        findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    openPopup(v);
                }
            }
        });
        findViewById(R.id.grid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    if (Reddit.tabletUI) {
                        if (posts.posts != null && !posts.posts.isEmpty()) {
                            DataShare.sharedSubreddit = posts.posts;
                            Intent i = new Intent(SubredditView.this, Shadowbox.class);
                            i.putExtra("position", 0);
                            startActivity(i);
                        }
                    } else {
                        new AlertDialogWrapper.Builder(SubredditView.this)
                                .setTitle(R.string.general_pro)
                                .setMessage(R.string.general_pro_msg)
                                .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                        }
                                    }
                                }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).show();
                    }
                }
            }
        });
        findViewById(R.id.info).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ToastHelpCreation.makeToast(v, getString(R.string.general_open_settings), SubredditView.this);
                return false;
            }
        });
        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                {
                    if (!subreddit.equals("frontpage") && !subreddit.equals("all")) {
                        ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(Gravity.RIGHT);
                    }

                }
            }
        });
    }

    private void openPopup(View view) {

        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        Reddit.defaultSorting = Sorting.HOT;
                        reloadSubs();
                        break;
                    case 1:
                        Reddit.defaultSorting = Sorting.NEW;


                        reloadSubs();
                        break;
                    case 2:
                        Reddit.defaultSorting = Sorting.RISING;


                        reloadSubs();
                        break;
                    case 3:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.HOUR;


                        reloadSubs();
                        break;
                    case 4:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.DAY;


                        reloadSubs();
                        break;
                    case 5:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.WEEK;
                        //TODO WEEK


                        reloadSubs();
                        break;
                    case 6:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.MONTH;

                        reloadSubs();
                        break;
                    case 7:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.YEAR;


                        reloadSubs();
                        break;
                    case 8:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.ALL;


                        reloadSubs();
                        break;
                    case 9:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.HOUR;


                        reloadSubs();
                        break;
                    case 10:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.DAY;


                        reloadSubs();
                        break;
                }

                SettingValues.defaultSorting = Reddit.defaultSorting;
                SettingValues.timePeriod = Reddit.timePeriod;
            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditView.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(
                Reddit.getSortingStrings(getBaseContext()), Reddit.getSortingId(), l2);
        builder.show();
    }

    private void reloadSubs() {
        restartTheme();
    }

    private void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
        if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
            findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

            final String text = subreddit.getDataNode().get("description_html").asText();
            final ActiveTextView body = (ActiveTextView) findViewById(R.id.sidebar_text);
            new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, SubredditView.this, subreddit.getDisplayName());
        } else {
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
        }
        {
            CheckBox c = ((CheckBox) findViewById(R.id.subscribed));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //reset check adapter
                }
            });
            if (SubredditStorage.realSubs != null)
                c.setChecked(SubredditStorage.realSubs.contains(subreddit.getDisplayName().toLowerCase()));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public void onPostExecute(Void voids) {
                            new SubredditStorageNoContext().execute(SubredditView.this);
                            Snackbar.make(rv, isChecked ? getString(R.string.misc_subscribed) :
                                    getString(R.string.misc_unsubscribed), Snackbar.LENGTH_SHORT);
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            if (isChecked) {
                                new AccountManager(Authentication.reddit).subscribe(subreddit);
                            } else {
                                new AccountManager(Authentication.reddit).unsubscribe(subreddit);

                            }
                            return null;
                        }
                    }.execute();

                }
            });
        }
        ((TextView) findViewById(R.id.sub_title)).setText(subreddit.getPublicDescription());
        findViewById(R.id.sub_title).setVisibility(subreddit.getPublicDescription().equals("") ? View.GONE : View.VISIBLE);

        ((TextView) findViewById(R.id.subscribers)).setText(getString(R.string.subreddit_subscribers, subreddit.getSubscriberCount()));
        findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

    }

    private void doSubSidebar(final String subreddit) {
        if (!subreddit.equals("all") && !subreddit.equals("frontpage")) {
            if (drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);

            new AsyncGetSubreddit().execute(subreddit);
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
            findViewById(R.id.sub_title).setVisibility(View.GONE);
            findViewById(R.id.subscribers).setVisibility(View.GONE);


            findViewById(R.id.header_sub).setBackgroundColor(Palette.getColor(subreddit));
            ((TextView) findViewById(R.id.sub_infotitle)).setText(subreddit);
            View dialoglayout = findViewById(R.id.sidebarsub);
            {
                CheckBox pinned = ((CheckBox) dialoglayout.findViewById(R.id.pinned));
                Button submit = ((Button) dialoglayout.findViewById(R.id.submit));
                if (!Authentication.isLoggedIn) {
                    pinned.setVisibility(View.GONE);
                    findViewById(R.id.subscribed).setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);
                }
                if (Reddit.fab && Reddit.fabType == R.integer.FAB_POST)
                    submit.setVisibility(View.GONE);

                pinned.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //reset check adapter
                    }
                });
                if (SubredditStorage.getPins() == null) {
                    pinned.setChecked(false);

                } else if (SubredditStorage.getPins().contains(subreddit.toLowerCase())) {
                    pinned.setChecked(true);
                } else {
                    pinned.setChecked(false);
                }
                pinned.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            SubredditStorage.addPin(subreddit);
                        } else {
                            SubredditStorage.removePin(subreddit);
                        }
                    }
                });
                pinned.setHighlightColor(new ColorPreferences(SubredditView.this).getThemeSubreddit(subreddit, true).getColor());

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(SubredditView.this, Submit.class);
                        inte.putExtra("subreddit", subreddit);
                        SubredditView.this.startActivity(inte);
                    }
                });
            }


            if (subreddit.toLowerCase().equals("frontpage") || subreddit.toLowerCase().equals("all")) {
                dialoglayout.findViewById(R.id.wiki).setVisibility(View.GONE);
                dialoglayout.findViewById(R.id.sidebar_text).setVisibility(View.GONE);

            } else {
                dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(SubredditView.this, Wiki.class);
                        i.putExtra("subreddit", subreddit);
                        startActivity(i);
                    }
                });

            }
            findViewById(R.id.sub_theme).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    int style = new ColorPreferences(SubredditView.this).getThemeSubreddit(subreddit);
                    final Context contextThemeWrapper = new ContextThemeWrapper(SubredditView.this, style);
                    LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                    final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditView.this);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setText("/r/" + subreddit);
                    title.setBackgroundColor(Palette.getColor(subreddit));

                    {
                        final View body = dialoglayout.findViewById(R.id.body2);

                        LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker);
                        final LineColorPicker colorPicker2 = (LineColorPicker) dialoglayout.findViewById(R.id.picker2);

                        colorPicker.setColors(new int[]{
                                getResources().getColor(R.color.md_red_500),
                                getResources().getColor(R.color.md_pink_500),
                                getResources().getColor(R.color.md_purple_500),
                                getResources().getColor(R.color.md_deep_purple_500),
                                getResources().getColor(R.color.md_indigo_500),
                                getResources().getColor(R.color.md_blue_500),
                                getResources().getColor(R.color.md_light_blue_500),
                                getResources().getColor(R.color.md_cyan_500),
                                getResources().getColor(R.color.md_teal_500),
                                getResources().getColor(R.color.md_green_500),
                                getResources().getColor(R.color.md_light_green_500),
                                getResources().getColor(R.color.md_lime_500),
                                getResources().getColor(R.color.md_yellow_500),
                                getResources().getColor(R.color.md_amber_500),
                                getResources().getColor(R.color.md_orange_500),
                                getResources().getColor(R.color.md_deep_orange_500),
                                getResources().getColor(R.color.md_brown_500),
                                getResources().getColor(R.color.md_grey_500),
                                getResources().getColor(R.color.md_blue_grey_500),

                        });
                        int currentColor = Palette.getColor(subreddit);
                        for (int i : colorPicker.getColors()) {
                            for (int i2 : ColorPreferences.getColors(getBaseContext(), i)) {
                                if (i2 == currentColor) {
                                    colorPicker.setSelectedColor(i);
                                    colorPicker2.setColors(ColorPreferences.getColors(getBaseContext(), i));
                                    colorPicker2.setSelectedColor(i2);
                                    break;
                                }
                            }
                        }
                        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int c) {

                                colorPicker2.setColors(ColorPreferences.getColors(getBaseContext(), c));
                                colorPicker2.setSelectedColor(c);


                            }
                        });
                        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int i) {
                                findViewById(R.id.header).setBackgroundColor(colorPicker2.getColor());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Window window = getWindow();
                                    window.setStatusBarColor(Palette.getDarkerColor(colorPicker2.getColor()));
                                    SubredditView.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

                                }
                                findViewById(R.id.header_sub).setBackgroundColor(colorPicker2.getColor());

                                title.setBackgroundColor(colorPicker2.getColor());
                            }
                        });
                        final LineColorPicker colorPickeracc = (LineColorPicker) dialoglayout.findViewById(R.id.picker3);


                        {
                         /* TODO   TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.reset);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Palette.removeColor(subreddit);
                                    hea.setBackgroundColor(Palette.getDefaultColor());
                                    findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Window window = getWindow();
                                        window.setStatusBarColor(Palette.getDarkerColor(Palette.getDefaultColor()));
                                        MainActivity.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

                                    }
                                    title.setBackgroundColor(Palette.getDefaultColor());


                                    int cx = center.getWidth() / 2;
                                    int cy = center.getHeight() / 2;

                                    int initialRadius = body.getWidth();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                        Animator anim =
                                                ViewAnimationUtils.createCircularReveal(body, cx, cy, initialRadius, 0);

                                        anim.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                body.setVisibility(View.GONE);
                                            }
                                        });
                                        anim.start();

                                    } else {
                                        body.setVisibility(View.GONE);

                                    }

                                }
                            });*/


                        }
                        final RadioButton def = (RadioButton) dialoglayout.findViewById(R.id.def);
                        final RadioButton alt = (RadioButton) dialoglayout.findViewById(R.id.alt);


                        {


                            int[] arrs = new int[ColorPreferences.Theme.values().length / 3];
                            int i = 0;
                            for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                                if (type.getThemeType() == 0) {
                                    arrs[i] = getResources().getColor(type.getColor());

                                    i++;
                                }
                            }

                            colorPickeracc.setColors(arrs);

                            colorPickeracc.setColors(arrs);

                            int topick = new ColorPreferences(SubredditView.this).getFontStyleSubreddit(subreddit).getColor();
                            for(int color : arrs){
                                if(color == topick){
                                    colorPickeracc.setSelectedColorPosition(color);
                                    break;

                                }
                            }

                        }


                        int i = (SettingValues.prefs.contains("PRESET" + subreddit) ? 1 : 0);
                        if (i == 0) {
                            def.setChecked(true);
                        } else {
                            alt.setChecked(true);
                        }


                        def.setText(R.string.settings_layout_default);
                        alt.setText(R.string.settings_title_alternative_layout);


                        builder.setView(dialoglayout);
                       final Dialog diag = builder.show();

                        {
                            TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Palette.setColor(subreddit, colorPicker2.getColor());
                                    int color = colorPickeracc.getColor();
                                    ColorPreferences.Theme t = null;
                                    for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                                        if (getResources().getColor(type.getColor()) == color && Reddit.themeBack == type.getThemeType()) {
                                            t = type;
                                            break;
                                        }
                                    }

                                    new ColorPreferences(SubredditView.this).setFontStyle(t, subreddit);

                                    if (alt.isChecked()) {
                                        SettingValues.prefs.edit().putBoolean("PRESET" + subreddit, true).apply();
                                    } else {
                                        SettingValues.prefs.edit().remove("PRESET" + subreddit).apply();
                                    }

                                    restartTheme();
                                    diag.dismiss();

                                }


                            });


                        }
                        {
                            TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.cancel);

                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    diag.dismiss();
                                    restartTheme();


                                }
                            });
                        }

                    }

                }
            });
        } else {
            if (drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        }
    }

    private class ShowPopupSidebar extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            final String text = Authentication.reddit.getSubreddit(params[0]).getDataNode().get("description_html").asText();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.justtext, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(SubredditView.this);
                    final ActiveTextView body = (ActiveTextView) dialoglayout.findViewById(R.id.body);
                    new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, SubredditView.this, subreddit);

                    builder.setView(dialoglayout).show();

                }
            });
            return null;
        }
    }

    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(Subreddit subreddit) {
            if (subreddit != null)
                doSubOnlyStuff(subreddit);
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            try {
                return Authentication.reddit.getSubreddit(params[0]);
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialogWrapper.Builder(SubredditView.this)
                                    .setTitle(R.string.subreddit_err)
                                    .setMessage(getString(R.string.subreddit_err_msg, params[0]))
                                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            setResult(4);
                                            finish();
                                        }
                                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    setResult(4);
                                    finish();
                                }
                            }).show();
                        } catch (Exception e) {

                        }
                    }
                });

                return null;
            }
        }
    }


}