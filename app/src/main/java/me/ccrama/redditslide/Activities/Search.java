package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.TimePeriod;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Adapters.ContributionAdapter;
import me.ccrama.redditslide.Adapters.SubredditSearchPosts;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;
import me.ccrama.redditslide.util.LogUtil;

public class Search extends BaseActivityAnim {


    public static final String EXTRA_TERM = "term";
    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_SITE = "site";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_SELF = "self";
    public static final String EXTRA_NSFW = "nsfw";
    public static final String EXTRA_AUTHOR = "author";

    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private ContributionAdapter adapter;

    private String where;
    private String subreddit;
    private String site;
    private String url;
    private boolean self;
    private boolean nsfw;
    private String author;

    private SubredditSearchPosts posts;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    public void reloadSubs() {
        posts.refreshLayout.setRefreshing(true);
        posts.reset();


    }

    public void openPopup() {

        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {

                    case 0:
                        time = TimePeriod.HOUR;
                        break;
                    case 1:
                        time = TimePeriod.DAY;
                        break;
                    case 2:
                        time = TimePeriod.WEEK;
                        break;
                    case 3:
                        time = TimePeriod.MONTH;
                        break;
                    case 4:
                        time = TimePeriod.YEAR;
                        break;
                    case 5:
                        time = TimePeriod.ALL;
                        break;

                }
                reloadSubs();
                getSupportActionBar().setSubtitle(Reddit.getSortingStringsSearch(getBaseContext())[Reddit.getSortingIdSearch(Search.this)]);

            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Search.this);
        builder.setTitle(R.string.sorting_time_choose);
        builder.setSingleChoiceItems(Reddit.getSortingStringsSearch(getBaseContext()), Reddit.getSortingIdSearch(this), l2);
        builder.show();

    }

    public void openPopup2() {

        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {

                    case 0:
                        Reddit.search = SubmissionSearchPaginator.SearchSort.RELEVANCE;
                        break;
                    case 1:
                        Reddit.search = SubmissionSearchPaginator.SearchSort.TOP;
                        break;
                    case 2:
                        Reddit.search = SubmissionSearchPaginator.SearchSort.NEW;
                        break;
                    case 3:
                        Reddit.search = SubmissionSearchPaginator.SearchSort.COMMENTS;
                        break;


                }
                reloadSubs();

            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Search.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(Reddit.getSearch(getBaseContext()), Reddit.getTypeSearch(), l2);
        builder.show();

    }

    public TimePeriod time;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                onBackPressed();
                return true;
            case R.id.time:
                openPopup();
                return true;
            case R.id.sort:
                openPopup2();
                return true;

        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);
        applyColorTheme("");
        setContentView(R.layout.activity_saved);
        where = getIntent().getExtras().getString(EXTRA_TERM, "");

        if (getIntent().hasExtra(EXTRA_AUTHOR)) {
            where = where + "&author=" + getIntent().getExtras().getString(EXTRA_AUTHOR);
        }
        if (getIntent().hasExtra(EXTRA_NSFW)) {
            where = where + "&nsfw=" + (getIntent().getExtras().getBoolean(EXTRA_NSFW)?"yes":"no");
        }
        if (getIntent().hasExtra(EXTRA_SELF)) {
            where = where + "&selftext=" + (getIntent().getExtras().getBoolean(EXTRA_SELF)?"yes":"no");
        }
        if (getIntent().hasExtra(EXTRA_SITE)) {
            where = where + "&site=" + getIntent().getExtras().getString(EXTRA_SITE);
        }
        if (getIntent().hasExtra(EXTRA_URL)) {
            where = where + "&url=" + getIntent().getExtras().getString(EXTRA_URL);
        }

        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");
        setupSubredditAppBar(R.id.toolbar, "Search", true, subreddit.toLowerCase());

        Log.v(LogUtil.getTag(), "Searching for " + where + " in " + subreddit);

        time = TimePeriod.ALL;

        getSupportActionBar().setTitle(where);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        assert mToolbar != null; //it won't be, trust me
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); //Simulate a system's "Back" button functionality.
            }
        });
        getSupportActionBar().setSubtitle(Reddit.getSortingStringsSearch(getBaseContext())[Reddit.getSortingIdSearch(this)]);
        final RecyclerView rv = ((RecyclerView) findViewById(R.id.vertical_content));
        final PreCachingLayoutManager mLayoutManager;
        mLayoutManager = new PreCachingLayoutManager(this);
        rv.setLayoutManager(mLayoutManager);

        rv.addOnScrollListener(new ToolbarScrollHideHandler(mToolbar, findViewById(R.id.header)) {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

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
                    if ((visibleItemCount + pastVisiblesItems) + 5>= totalItemCount) {
                        posts.loading = true;
                        posts.loadMore(adapter, subreddit, where, false);

                    }
                }
            }
        });
        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(subreddit, this));

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we just do 7% of the device screen height as a general estimate for just a toolbar
        int screenHeight = this.getResources().getDisplayMetrics().heightPixels;
        int headerOffset = Math.round((float) (screenHeight * 0.07));

        mSwipeRefreshLayout.setProgressViewOffset(false,
                headerOffset - Reddit.pxToDp(42, Search.this),
                headerOffset + Reddit.pxToDp(42, Search.this));

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        posts = new SubredditSearchPosts(subreddit, where.toLowerCase(), this);
        adapter = new ContributionAdapter(this, posts, rv);
        rv.setAdapter(adapter);


        try {
            posts.bindAdapter(adapter, mSwipeRefreshLayout);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        //TODO catch errors
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        posts.loadMore(adapter, subreddit, where, true);

                        //TODO catch errors
                    }
                }
        );
    }


}