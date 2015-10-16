package me.ccrama.redditslide.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

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
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.SubredditStorageNoContext;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.ToastHelpCreation;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

public class SubredditView extends BaseActivity {


    public DrawerLayout drawerLayout;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView rv;
    public class ShowPopupSidebar extends AsyncTask<String, Void, Void> {

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
    public void restartTheme() {
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
            adapter = new SubmissionAdapter(this, posts, rv, subreddit );
            rv.setAdapter(adapter);
        } else if (requestCode == 1) {
            restartTheme();
        }
    }
    public String subreddit;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        subreddit = getIntent().getExtras().getString("subreddit", "");

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(subreddit, true).getBaseId(), true);

        setContentView(R.layout.activity_singlesubreddit);

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setBackgroundColor(Pallete.getColor(subreddit));
        setSupportActionBar(t);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDarkerColor(subreddit)));
        }
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        getSupportActionBar().setTitle(subreddit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rv = ((RecyclerView) findViewById(R.id.vertical_content));
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE || ! Reddit.tabletUI) {
            final PreCachingLayoutManager mLayoutManager;
            mLayoutManager = new PreCachingLayoutManager(this);
            rv.setLayoutManager(mLayoutManager);
        } else {
            final StaggeredGridLayoutManager mLayoutManager;
            mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
            rv.setLayoutManager(mLayoutManager);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        mSwipeRefreshLayout.setColorSchemeColors(Pallete.getColors(subreddit, this));

        mSwipeRefreshLayout.setRefreshing(true);
        posts = new SubredditPosts(subreddit);
        adapter = new SubmissionAdapter(this, posts, rv, subreddit );
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
                        try {
                            posts.loadMore(adapter, true, subreddit);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
                    if(Reddit.tabletUI) {
                        DataShare.sharedSubreddit = posts.posts;
                        Intent i = new Intent(SubredditView.this, Shadowbox.class);
                        i.putExtra("position", 0);
                        startActivity(i);
                    } else {
                    new AlertDialogWrapper.Builder(SubredditView.this)
                            .setTitle("Slide for Reddit Pro")
                            .setMessage("I have opted to make a few features of Slide (including multi-column mode) unlockable by purchasing a Pro Unlock key from the Play Store. \n\n" +
                                    "This is to keep development going, and in leiu of displaying ads in the free version of Slide!\n\n" +
                                    "Included in this is MultiColumn mode, Shadowbox mode (for image subreddits), and much more coming soon!\n\n" +
                                    "Would you like to unlock Slide for Reddit Pro?")
                            .setPositiveButton("Sure!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    }
                                }
                            }).setNegativeButton("No thank you", new DialogInterface.OnClickListener() {
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
                ToastHelpCreation.makeToast(v, "Open Subreddit Settings", SubredditView.this);
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
    public int[] getColors(int c) {
        if (c == getResources().getColor(R.color.md_red_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_red_100),
                    getResources().getColor(R.color.md_red_200),
                    getResources().getColor(R.color.md_red_300),
                    getResources().getColor(R.color.md_red_400),
                    getResources().getColor(R.color.md_red_500),
                    getResources().getColor(R.color.md_red_600),
                    getResources().getColor(R.color.md_red_700),
                    getResources().getColor(R.color.md_red_800),
                    getResources().getColor(R.color.md_red_900)
            };
        } else if (c == getResources().getColor(R.color.md_pink_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_pink_100),
                    getResources().getColor(R.color.md_pink_200),
                    getResources().getColor(R.color.md_pink_300),
                    getResources().getColor(R.color.md_pink_400),
                    getResources().getColor(R.color.md_pink_500),
                    getResources().getColor(R.color.md_pink_600),
                    getResources().getColor(R.color.md_pink_700),
                    getResources().getColor(R.color.md_pink_800),
                    getResources().getColor(R.color.md_pink_900)
            };
        } else if (c == getResources().getColor(R.color.md_purple_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_purple_100),
                    getResources().getColor(R.color.md_purple_200),
                    getResources().getColor(R.color.md_purple_300),
                    getResources().getColor(R.color.md_purple_400),
                    getResources().getColor(R.color.md_purple_500),
                    getResources().getColor(R.color.md_purple_600),
                    getResources().getColor(R.color.md_purple_700),
                    getResources().getColor(R.color.md_purple_800),
                    getResources().getColor(R.color.md_purple_900)
            };
        } else if (c == getResources().getColor(R.color.md_deep_purple_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_deep_purple_100),
                    getResources().getColor(R.color.md_deep_purple_200),
                    getResources().getColor(R.color.md_deep_purple_300),
                    getResources().getColor(R.color.md_deep_purple_400),
                    getResources().getColor(R.color.md_deep_purple_500),
                    getResources().getColor(R.color.md_deep_purple_600),
                    getResources().getColor(R.color.md_deep_purple_700),
                    getResources().getColor(R.color.md_deep_purple_800),
                    getResources().getColor(R.color.md_deep_purple_900)
            };
        } else if (c == getResources().getColor(R.color.md_indigo_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_indigo_100),
                    getResources().getColor(R.color.md_indigo_200),
                    getResources().getColor(R.color.md_indigo_300),
                    getResources().getColor(R.color.md_indigo_400),
                    getResources().getColor(R.color.md_indigo_500),
                    getResources().getColor(R.color.md_indigo_600),
                    getResources().getColor(R.color.md_indigo_700),
                    getResources().getColor(R.color.md_indigo_800),
                    getResources().getColor(R.color.md_indigo_900)
            };
        } else if (c == getResources().getColor(R.color.md_blue_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_blue_100),
                    getResources().getColor(R.color.md_blue_200),
                    getResources().getColor(R.color.md_blue_300),
                    getResources().getColor(R.color.md_blue_400),
                    getResources().getColor(R.color.md_blue_500),
                    getResources().getColor(R.color.md_blue_600),
                    getResources().getColor(R.color.md_blue_700),
                    getResources().getColor(R.color.md_blue_800),
                    getResources().getColor(R.color.md_blue_900)
            };
        } else if (c == getResources().getColor(R.color.md_light_blue_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_light_blue_100),
                    getResources().getColor(R.color.md_light_blue_200),
                    getResources().getColor(R.color.md_light_blue_300),
                    getResources().getColor(R.color.md_light_blue_400),
                    getResources().getColor(R.color.md_light_blue_500),
                    getResources().getColor(R.color.md_light_blue_600),
                    getResources().getColor(R.color.md_light_blue_700),
                    getResources().getColor(R.color.md_light_blue_800),
                    getResources().getColor(R.color.md_light_blue_900)
            };
        } else if (c == getResources().getColor(R.color.md_cyan_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_cyan_100),
                    getResources().getColor(R.color.md_cyan_200),
                    getResources().getColor(R.color.md_cyan_300),
                    getResources().getColor(R.color.md_cyan_400),
                    getResources().getColor(R.color.md_cyan_500),
                    getResources().getColor(R.color.md_cyan_600),
                    getResources().getColor(R.color.md_cyan_700),
                    getResources().getColor(R.color.md_cyan_800),
                    getResources().getColor(R.color.md_cyan_900)
            };
        } else if (c == getResources().getColor(R.color.md_teal_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_teal_100),
                    getResources().getColor(R.color.md_teal_200),
                    getResources().getColor(R.color.md_teal_300),
                    getResources().getColor(R.color.md_teal_400),
                    getResources().getColor(R.color.md_teal_500),
                    getResources().getColor(R.color.md_teal_600),
                    getResources().getColor(R.color.md_teal_700),
                    getResources().getColor(R.color.md_teal_800),
                    getResources().getColor(R.color.md_teal_900)
            };
        } else if (c == getResources().getColor(R.color.md_green_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_green_100),
                    getResources().getColor(R.color.md_green_200),
                    getResources().getColor(R.color.md_green_300),
                    getResources().getColor(R.color.md_green_400),
                    getResources().getColor(R.color.md_green_500),
                    getResources().getColor(R.color.md_green_600),
                    getResources().getColor(R.color.md_green_700),
                    getResources().getColor(R.color.md_green_800),
                    getResources().getColor(R.color.md_green_900)
            };
        } else if (c == getResources().getColor(R.color.md_light_green_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_light_green_100),
                    getResources().getColor(R.color.md_light_green_200),
                    getResources().getColor(R.color.md_light_green_300),
                    getResources().getColor(R.color.md_light_green_400),
                    getResources().getColor(R.color.md_light_green_500),
                    getResources().getColor(R.color.md_light_green_600),
                    getResources().getColor(R.color.md_light_green_700),
                    getResources().getColor(R.color.md_light_green_800),
                    getResources().getColor(R.color.md_light_green_900)
            };
        } else if (c == getResources().getColor(R.color.md_lime_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_lime_100),
                    getResources().getColor(R.color.md_lime_200),
                    getResources().getColor(R.color.md_lime_300),
                    getResources().getColor(R.color.md_lime_400),
                    getResources().getColor(R.color.md_lime_500),
                    getResources().getColor(R.color.md_lime_600),
                    getResources().getColor(R.color.md_lime_700),
                    getResources().getColor(R.color.md_lime_800),
                    getResources().getColor(R.color.md_lime_900)
            };
        } else if (c == getResources().getColor(R.color.md_yellow_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_yellow_100),
                    getResources().getColor(R.color.md_yellow_200),
                    getResources().getColor(R.color.md_yellow_300),
                    getResources().getColor(R.color.md_yellow_400),
                    getResources().getColor(R.color.md_yellow_500),
                    getResources().getColor(R.color.md_yellow_600),
                    getResources().getColor(R.color.md_yellow_700),
                    getResources().getColor(R.color.md_yellow_800),
                    getResources().getColor(R.color.md_yellow_900)
            };
        } else if (c == getResources().getColor(R.color.md_amber_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_amber_100),
                    getResources().getColor(R.color.md_amber_200),
                    getResources().getColor(R.color.md_amber_300),
                    getResources().getColor(R.color.md_amber_400),
                    getResources().getColor(R.color.md_amber_500),
                    getResources().getColor(R.color.md_amber_600),
                    getResources().getColor(R.color.md_amber_700),
                    getResources().getColor(R.color.md_amber_800),
                    getResources().getColor(R.color.md_amber_900)
            };
        } else if (c == getResources().getColor(R.color.md_orange_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_orange_100),
                    getResources().getColor(R.color.md_orange_200),
                    getResources().getColor(R.color.md_orange_300),
                    getResources().getColor(R.color.md_orange_400),
                    getResources().getColor(R.color.md_orange_500),
                    getResources().getColor(R.color.md_orange_600),
                    getResources().getColor(R.color.md_orange_700),
                    getResources().getColor(R.color.md_orange_800),
                    getResources().getColor(R.color.md_orange_900)
            };
        } else if (c == getResources().getColor(R.color.md_deep_orange_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_deep_orange_100),
                    getResources().getColor(R.color.md_deep_orange_200),
                    getResources().getColor(R.color.md_deep_orange_300),
                    getResources().getColor(R.color.md_deep_orange_400),
                    getResources().getColor(R.color.md_deep_orange_500),
                    getResources().getColor(R.color.md_deep_orange_600),
                    getResources().getColor(R.color.md_deep_orange_700),
                    getResources().getColor(R.color.md_deep_orange_800),
                    getResources().getColor(R.color.md_deep_orange_900)
            };
        } else if (c == getResources().getColor(R.color.md_brown_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_brown_100),
                    getResources().getColor(R.color.md_brown_200),
                    getResources().getColor(R.color.md_brown_300),
                    getResources().getColor(R.color.md_brown_400),
                    getResources().getColor(R.color.md_brown_500),
                    getResources().getColor(R.color.md_brown_600),
                    getResources().getColor(R.color.md_brown_700),
                    getResources().getColor(R.color.md_brown_800),
                    getResources().getColor(R.color.md_brown_900)
            };
        } else if (c == getResources().getColor(R.color.md_grey_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_grey_100),
                    getResources().getColor(R.color.md_grey_200),
                    getResources().getColor(R.color.md_grey_300),
                    getResources().getColor(R.color.md_grey_400),
                    getResources().getColor(R.color.md_grey_500),
                    getResources().getColor(R.color.md_grey_600),
                    getResources().getColor(R.color.md_grey_700),
                    getResources().getColor(R.color.md_grey_800),
                    getResources().getColor(R.color.md_grey_900)
            };
        } else {
            return new int[]{
                    getResources().getColor(R.color.md_blue_grey_100),
                    getResources().getColor(R.color.md_blue_grey_200),
                    getResources().getColor(R.color.md_blue_grey_300),
                    getResources().getColor(R.color.md_blue_grey_400),
                    getResources().getColor(R.color.md_blue_grey_500),
                    getResources().getColor(R.color.md_blue_grey_600),
                    getResources().getColor(R.color.md_blue_grey_700),
                    getResources().getColor(R.color.md_blue_grey_800),
                    getResources().getColor(R.color.md_blue_grey_900)
            };

        }
    }

    public SubmissionAdapter adapter;

    public SubredditPosts posts;


    public void openPopup(View view) {

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
            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditView.this);
        builder.setTitle("Choose a Sorting Type");
        builder.setItems(
                new String[]{"Hot", "New", "Rising", "Top This Hour", "Top Today", "Top This Week", "Top This Month", "Top This Year", "Top All Time", "Controversial This Hour", "Controversial Today"}, l2);
        builder.show();

    }
    public void reloadSubs() {
restartTheme();
    }
    public void doSubOnlyStuff(Subreddit subreddit) {
        if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
            final String text = subreddit.getDataNode().get("description_html").asText();
            final ActiveTextView body = (ActiveTextView) findViewById(R.id.sidebar_text);
            new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, SubredditView.this, "slideforreddit");
        } else {
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
        }
        ((TextView) findViewById(R.id.sub_title)).setText(subreddit.getPublicDescription());

        ((TextView) findViewById(R.id.subscribers)).setText("" + subreddit.getSubscriberCount() + " subscribers");

    }

    public class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(Subreddit subreddit) {
            doSubOnlyStuff(subreddit);
        }

        @Override
        protected Subreddit doInBackground(String... params) {
            return Authentication.reddit.getSubreddit(params[0]);
        }
    }

    public void doSubSidebar(final String subreddit){
        if(!subreddit.equals("all") && !subreddit.equals("frontpage")) {
            if(drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);

            new AsyncGetSubreddit().execute(subreddit);
            findViewById(R.id.header_sub).setBackgroundColor(Pallete.getColor(subreddit));
            ((TextView)findViewById(R.id.sub_infotitle)).setText(subreddit);
            View dialoglayout = findViewById(R.id.sidebarsub);
            CheckBox c = ((CheckBox) dialoglayout.findViewById(R.id.pinned));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //reset check adapter
                }
            });
            if (SubredditStorage.getPins() == null) {
                c.setChecked(false);

            } else if (SubredditStorage.getPins().contains(subreddit.toLowerCase())) {
                c.setChecked(true);
            } else {
                c.setChecked(false);
            }
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        SubredditStorage.addPin(subreddit);
                    } else {
                        SubredditStorage.removePin(subreddit);
                    }

                    new SubredditStorageNoContext().execute(SubredditView.this);
                }
            });
            c.setHighlightColor(new ColorPreferences(SubredditView.this).getThemeSubreddit(subreddit, true).getColor());


            if(subreddit.toLowerCase().equals("frontpage") || subreddit.toLowerCase().equals("all") ){
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
                    Log.v("Slide", "STYLE: " + style + " DEFAULT: " + R.style.deeporange_dark);
                    LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                    final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditView.this);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setText("/r/" + subreddit);
                    title.setBackgroundColor(Pallete.getColor(subreddit));

                    {
                        final View body = dialoglayout.findViewById(R.id.body2);
                        body.setVisibility(View.INVISIBLE);
                        final View center = dialoglayout.findViewById(R.id.colorExpandFrom);
                        dialoglayout.findViewById(R.id.color).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int cx = center.getWidth() / 2;
                                int cy = center.getHeight() / 2;

                                int finalRadius = Math.max(body.getWidth(), body.getHeight());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Animator anim =
                                            ViewAnimationUtils.createCircularReveal(body, cx, cy, 0, finalRadius);
                                    body.setVisibility(View.VISIBLE);
                                    anim.start();
                                } else {
                                    body.setVisibility(View.VISIBLE);
                                }
                            }
                        });

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

                        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int c) {

                                colorPicker2.setColors(getColors(c));
                                colorPicker2.setSelectedColor(c);


                            }
                        });
                        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int i) {
                                findViewById(R.id.header).setBackgroundColor(colorPicker2.getColor());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Window window = getWindow();
                                    window.setStatusBarColor(Pallete.getDarkerColor(colorPicker2.getColor()));
                                    SubredditView.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

                                }
                                title.setBackgroundColor(colorPicker2.getColor());
                            }
                        });


                        {
                            TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.reset);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Pallete.removeColor(subreddit);
                                    findViewById(R.id.header).setBackgroundColor(Pallete.getDefaultColor());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Window window = getWindow();
                                        window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
                                        SubredditView.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

                                    }
                                    title.setBackgroundColor(Pallete.getDefaultColor());


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
                            });


                        }
                        {
                            TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Pallete.setColor(subreddit, colorPicker2.getColor());

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
                            });


                        }
                    }
                    {
                        {

                            final View body = dialoglayout.findViewById(R.id.body3);
                            body.setVisibility(View.INVISIBLE);
                            final View center = dialoglayout.findViewById(R.id.colorExpandFrom2);
                            dialoglayout.findViewById(R.id.color2).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    int cx = center.getWidth() / 2;
                                    int cy = center.getHeight() / 2;

                                    int finalRadius = Math.max(body.getWidth(), body.getHeight());

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Animator anim =
                                                ViewAnimationUtils.createCircularReveal(body, cx, cy, 0, finalRadius);
                                        body.setVisibility(View.VISIBLE);
                                        anim.start();
                                    } else {
                                        body.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                            final LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker3);

                            int[] arrs = new int[ColorPreferences.Theme.values().length / 3];
                            int i = 0;
                            for(ColorPreferences.Theme type : ColorPreferences.Theme.values()){
                                if(type.getThemeType() == 0) {
                                    arrs[i] = getResources().getColor(type.getColor());

                                    i++;
                                }
                            }

                            colorPicker.setColors(arrs);

                            colorPicker.setSelectedColor(new ColorPreferences(SubredditView.this).getFontStyle().getColor());

                            {
                                TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok2);

                                // if button is clicked, close the custom dialog
                                dialogButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int color = colorPicker.getColor();
                                        ColorPreferences.Theme t = null;
                                        for(ColorPreferences.Theme type : ColorPreferences.Theme.values()){
                                            if(getResources().getColor(type.getColor()) == color  && Reddit.themeBack == type.getThemeType()){
                                                t = type;
                                                break;
                                            }
                                        }

                                        new ColorPreferences(SubredditView.this).setFontStyle(t, subreddit);

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
                                });


                            }
                        }
                    }

                    dialoglayout.findViewById(R.id.card).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(SubredditView.this, EditCardsLayout.class);
                            i.putExtra("subreddit", subreddit);
                            startActivityForResult(i, 1);
                        }
                    });
                    builder.setView(dialoglayout);
                    builder.show();

                }
            });
        } else {
            if(drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        }
    }



}