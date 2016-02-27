package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import me.ccrama.redditslide.Adapters.SubmissionAdapter;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.SidebarLayout;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

public class SubredditView extends BaseActivityAnim implements SubmissionDisplay {

    public static final String EXTRA_SUBREDDIT = "subreddit";

    private DrawerLayout drawerLayout;
    private RecyclerView rv;
    private String subreddit;
    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private SubmissionAdapter adapter;
    private SubredditPosts posts;
    public SwipeRefreshLayout mSwipeRefreshLayout;

    private void restartTheme() {
        Intent intent = this.getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_single_subreddit, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mSwipeRefreshLayout.setRefreshing(true);
                posts = new SubredditPosts(subreddit);
                adapter = new SubmissionAdapter(this, posts, rv, subreddit);
                rv.setAdapter(adapter);
                posts.loadMore(mSwipeRefreshLayout.getContext(), this, true);
                return true;
            case R.id.action_sort:
                openPopup();
                return true;
            case R.id.action_info:
                drawerLayout.openDrawer(Gravity.RIGHT);
                return true;
            case R.id.search:
                MaterialDialog.Builder builder = new MaterialDialog.Builder(this).title(R.string.search_title)
                        .alwaysCallInputCallback()
                        .input(getString(R.string.search_msg), "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                term = charSequence.toString();
                            }
                        })
                        .positiveText(R.string.search_all)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                Intent i = new Intent(SubredditView.this, Search.class);
                                i.putExtra(Search.EXTRA_TERM, term);
                                startActivity(i);
                            }
                        });

                //Add "search current sub" if it is not frontpage/all/random
                if (!subreddit.equalsIgnoreCase("frontpage") && !subreddit.equalsIgnoreCase("all") && !subreddit.equalsIgnoreCase("random") && !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("mod")) {
                    builder.negativeText(getString(R.string.search_subreddit, subreddit))
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(SubredditView.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    i.putExtra(Search.EXTRA_SUBREDDIT, subreddit);
                                    Log.v(LogUtil.getTag(), "INTENT SHOWS " + term + " AND " + subreddit);
                                    startActivity(i);
                                }
                            });
                }
                builder.show();
                return true;
            case R.id.action_shadowbox:
                if (SettingValues.tabletUI) {
                    if (posts.posts != null && !posts.posts.isEmpty()) {
                        Intent i = new Intent(this, Shadowbox.class);
                        i.putExtra(Shadowbox.EXTRA_PAGE, 0);
                        i.putExtra(Shadowbox.EXTRA_SUBREDDIT, subreddit);
                        startActivity(i);
                    }
                } else {
                    new AlertDialogWrapper.Builder(this)
                            .setTitle(R.string.general_pro)
                            .setMessage(R.string.general_pro_msg)
                            .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    } catch (ActivityNotFoundException e) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    }
                                }
                            }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                return true;
            default:
                return false;
        }
    }

    public String term;

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
        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");
        applyColorTheme(subreddit);
        setContentView(R.layout.activity_singlesubreddit);
        setupSubredditAppBar(R.id.toolbar, subreddit, true, subreddit);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setResult(3);

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (!drawerLayout.isDrawerOpen(Gravity.RIGHT))
                    drawerLayout.openDrawer(Gravity.RIGHT);

            }

            @Override
            public void onDrawerOpened(View drawerView) {

                mHelper.getSwipeBackLayout().mDragHelper.override = false;


            }

            @Override
            public void onDrawerClosed(View drawerView) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();

                if (SettingValues.swipeAnywhere || overrideRedditSwipeAnywhere) {
                    if (overrideSwipeFromAnywhere) {
                        Log.v(LogUtil.getTag(), "WONT SWIPE FROM ANYWHERE");
                        mHelper.getSwipeBackLayout().mDragHelper.override = false;

                    } else {

                        Log.v(LogUtil.getTag(), "WILL SWIPE FROM ANYWHERE");

                        mHelper.getSwipeBackLayout().mDragHelper.override = true;

                        mHelper.getSwipeBackLayout().setEdgeSize(metrics.widthPixels);
                        Log.v(LogUtil.getTag(), "EDGE SIZE IS " + metrics.widthPixels);
                    }
                } else {
                    mHelper.getSwipeBackLayout().mDragHelper.override = false;


                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });


        rv = ((RecyclerView) findViewById(R.id.vertical_content));

        final RecyclerView.LayoutManager mLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
            mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        } else {
            mLayoutManager = new PreCachingLayoutManager(this);

        }
        rv.setLayoutManager(mLayoutManager);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);


        rv.addOnScrollListener(new ToolbarScrollHideHandler(mToolbar, findViewById(R.id.header)) {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = rv.getLayoutManager().getChildCount();
                totalItemCount = rv.getLayoutManager().getItemCount();
                if (rv.getLayoutManager() instanceof PreCachingLayoutManager) {
                    pastVisiblesItems = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                    if (SettingValues.scrollSeen) {
                        if (pastVisiblesItems > 0) {
                            HasSeen.addSeen(posts.posts.get(pastVisiblesItems - 1).getFullName());
                        }
                    }
                } else {
                    int[] firstVisibleItems = null;
                    firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                    if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                        pastVisiblesItems = firstVisibleItems[0];
                        if (SettingValues.scrollSeen) {
                            if (pastVisiblesItems > 0) {
                                HasSeen.addSeen(posts.posts.get(pastVisiblesItems - 1).getFullName());
                            }
                        }
                    }
                }

                if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                    Log.v(LogUtil.getTag(), "LOADING MORE" + totalItemCount);
                    posts.loading = true;
                    posts.loadMore(mSwipeRefreshLayout.getContext(), SubredditView.this, false, posts.subreddit);

                }
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(subreddit, this));

        mSwipeRefreshLayout.setProgressViewOffset(false, Reddit.pxToDp(56, SubredditView.this), Reddit.pxToDp(92, SubredditView.this));

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        posts = new SubredditPosts(subreddit);
        adapter = new SubmissionAdapter(this, posts, rv, subreddit);
        rv.setAdapter(adapter);

        doSubSidebar(subreddit);
        posts.loadMore(mSwipeRefreshLayout.getContext(), this, true);

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        posts.loadMore(mSwipeRefreshLayout.getContext(), SubredditView.this, true, subreddit);

                    }
                }
        );


    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        int currentOrientation = getResources().getConfiguration().orientation;


        if (rv.getLayoutManager() instanceof LinearLayoutManager && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            int i = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
            final RecyclerView.LayoutManager mLayoutManager;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
                mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
                adapter.spanned = true;
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
                mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                adapter.spanned = true;

            } else {
                mLayoutManager = new PreCachingLayoutManager(this);
                adapter.spanned = false;

            }

            rv.setLayoutManager(mLayoutManager);

            mLayoutManager.scrollToPosition(i);


        } else {
            int i = 0;
            final RecyclerView.LayoutManager mLayoutManager;

            if (rv.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                int[] firstVisibleItems = null;
                firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    i = firstVisibleItems[0];
                }
            } else {
                i = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            }
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
                mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
                adapter.spanned = true;
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
                mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                adapter.spanned = true;

            } else {
                mLayoutManager = new PreCachingLayoutManager(this);
                adapter.spanned = false;

            }
            rv.setLayoutManager(mLayoutManager);
            mLayoutManager.scrollToPosition(i);
        }
    }
    public void openPopup() {

        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        Reddit.setSorting(subreddit, Sorting.HOT);
                        reloadSubs();
                        break;
                    case 1:
                        Reddit.setSorting(subreddit, Sorting.NEW);
                        reloadSubs();
                        break;
                    case 2:
                        Reddit.setSorting(subreddit, Sorting.RISING);
                        reloadSubs();
                        break;
                    case 3:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.HOUR);
                        reloadSubs();
                        break;
                    case 4:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.DAY);
                        reloadSubs();
                        break;
                    case 5:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.WEEK);
                        reloadSubs();
                        break;
                    case 6:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.MONTH);
                        reloadSubs();
                        break;
                    case 7:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.YEAR);
                        reloadSubs();
                        break;
                    case 8:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.ALL);
                        reloadSubs();
                        break;
                    case 9:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.HOUR);
                        reloadSubs();
                        break;
                    case 10:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.DAY);
                        reloadSubs();
                        break;
                    case 11:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.WEEK);
                        reloadSubs();
                    case 12:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.MONTH);
                        reloadSubs();
                    case 13:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.YEAR);
                        reloadSubs();
                    case 14:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.ALL);
                        reloadSubs();
                }
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

    private void setViews(String rawHTML, String subreddit, SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subreddit);
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subreddit);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subreddit);
            }
            SidebarLayout sidebar = (SidebarLayout) findViewById(R.id.drawer_layout);
            for (int i = 0; i < commentOverflow.getChildCount(); i++) {
                View maybeScrollable = commentOverflow.getChildAt(i);
                if (maybeScrollable instanceof HorizontalScrollView) {
                    sidebar.addScrollable(maybeScrollable);
                }
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

    private void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
        if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
            findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

            final String text = subreddit.getDataNode().get("description_html").asText();
            final SpoilerRobotoTextView body = (SpoilerRobotoTextView) findViewById(R.id.sidebar_text);
            CommentOverflow overflow = (CommentOverflow) findViewById(R.id.commentOverflow);
            setViews(text, subreddit.getDisplayName(), body, overflow);
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
            if (SubredditStorage.subredditsForHome != null)
                c.setChecked(SubredditStorage.subredditsForHome.contains(subreddit.getDisplayName().toLowerCase()));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public void onPostExecute(Void voids) {
                            if (isChecked) {
                                SubredditStorage.addSubscription(subreddit.getDisplayName().toLowerCase(), null);
                            } else {
                                SubredditStorage.removeSubscription(subreddit.getDisplayName().toLowerCase(), null);

                            }
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
        if (!subreddit.getPublicDescription().isEmpty()) {
            findViewById(R.id.sub_title).setVisibility(View.VISIBLE);
            setViews(subreddit.getDataNode().get("public_description_html").asText(), subreddit.getDisplayName().toLowerCase(), ((SpoilerRobotoTextView) findViewById(R.id.sub_title)), (CommentOverflow) findViewById(R.id.sub_title_overflow));
        } else {
            findViewById(R.id.sub_title).setVisibility(View.GONE);
        }
        if (subreddit.getDataNode().has("icon_img") && !subreddit.getDataNode().get("icon_img").asText().isEmpty()) {
            ((Reddit) getApplication()).getImageLoader().displayImage(subreddit.getDataNode().get("icon_img").asText(), (ImageView) findViewById(R.id.subimage));
        } else {
            findViewById(R.id.subimage).setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.subscribers)).setText(getString(R.string.subreddit_subscribers, subreddit.getSubscriberCount()));
        findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

    }

    private void doSubSidebar(final String subreddit) {
        if (!subreddit.equalsIgnoreCase("all") && !subreddit.equalsIgnoreCase("frontpage") && !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("mod") && !subreddit.contains("+")) {
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
                if (SettingValues.fab && SettingValues.fabType == R.integer.FAB_POST)
                    submit.setVisibility(View.GONE);

                pinned.setVisibility(View.GONE);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(SubredditView.this, Submit.class);
                        inte.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                        SubredditView.this.startActivity(inte);
                    }
                });
            }


            if (subreddit.toLowerCase().equals("frontpage") || subreddit.toLowerCase().equals("all") || subreddit.toLowerCase().equals("friends") || subreddit.equalsIgnoreCase("mod") || subreddit.contains("+")) {
                dialoglayout.findViewById(R.id.wiki).setVisibility(View.GONE);
                dialoglayout.findViewById(R.id.sidebar_text).setVisibility(View.GONE);

            } else {
                dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(SubredditView.this, Wiki.class);
                        i.putExtra(Wiki.EXTRA_SUBREDDIT, subreddit);
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

                        colorPicker.setColors(ColorPreferences.getBaseColors(SubredditView.this));
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
                                }
                                setRecentBar(subreddit, colorPicker2.getColor());
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
                                        SubredditView.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

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

                        {
                            int[] arrs = new int[ColorPreferences.Theme.values().length / 4];
                            int i = 0;
                            for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                                if (type.getThemeType() == 0) {
                                    arrs[i] = ContextCompat.getColor(SubredditView.this, type.getColor());

                                    i++;
                                }
                            }

                            colorPickeracc.setColors(arrs);

                            colorPickeracc.setColors(arrs);

                            int topick = new ColorPreferences(SubredditView.this).getFontStyleSubreddit(subreddit).getColor();
                            for (int color : arrs) {
                                if (color == topick) {
                                    colorPickeracc.setSelectedColorPosition(color);
                                    break;

                                }
                            }

                        }


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
                                        if (ContextCompat.getColor(SubredditView.this, type.getColor()) == color && Reddit.themeBack == type.getThemeType()) {
                                            t = type;
                                            break;
                                        }
                                    }

                                    new ColorPreferences(SubredditView.this).setFontStyle(t, subreddit);


                                    SettingValues.prefs.edit().remove(Reddit.PREF_LAYOUT + subreddit).apply();

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

    @Override
    public void updateSuccess(final List<Submission> submissions, final int startIndex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (startIndex != -1) {
                    adapter.notifyItemRangeInserted(startIndex + 1, posts.posts.size());
                } else {
                    adapter.notifyDataSetChanged();
                }
                mSwipeRefreshLayout.setRefreshing(false);


            }
        });
    }

    @Override
    public void updateOffline(List<Submission> submissions, final long cacheTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(true);

            }
        });
    }

    @Override
    public void updateOfflineError() {
        adapter.setError(true);
    }

    @Override
    public void updateError() {
        adapter.setError(true);
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
                    final SpoilerRobotoTextView body = (SpoilerRobotoTextView) dialoglayout.findViewById(R.id.body);
                    final CommentOverflow overflow = (CommentOverflow) dialoglayout.findViewById(R.id.commentOverflow);
                    setViews(text, subreddit, body, overflow);

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