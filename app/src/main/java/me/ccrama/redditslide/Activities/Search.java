package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

public class Search extends BaseActivityAnim {


    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private ContributionAdapter adapter;
    private String where;
    private String subreddit;
    private SubredditSearchPosts posts;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }
    public void reloadSubs(){
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

            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Search.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(Reddit.getSortingStringsSearch(getBaseContext()), Reddit.getSortingIdSearch(), l2);
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
    public TimePeriod time = TimePeriod.MONTH;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

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
        where = getIntent().getExtras().getString("term", "");
        subreddit = getIntent().getExtras().getString("subreddit", "");
        setupUserAppBar(R.id.toolbar, "Search", true, subreddit.toLowerCase());

        Log.v("Slide", "Searching for " + where + " in " + subreddit);


        final RecyclerView rv = ((RecyclerView) findViewById(R.id.vertical_content));
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
                        posts.loadMore(adapter, subreddit, where, false);

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