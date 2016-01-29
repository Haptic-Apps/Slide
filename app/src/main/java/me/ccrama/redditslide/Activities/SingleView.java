package me.ccrama.redditslide.Activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;

import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.Adapters.SubmissionAdapter;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;

public class SingleView extends BaseActivityAnim implements SubmissionDisplay {

    private SubmissionAdapter adapter;
    private SubredditPosts posts;
    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String subreddit = getIntent().getExtras().getString("type", "");
        applyColorTheme(subreddit);
        setContentView(R.layout.activity_singlesubreddit);
        setupSubredditAppBar(R.id.toolbar, subreddit, true, subreddit);

        final RecyclerView rv = ((RecyclerView) findViewById(R.id.vertical_content));
        final StaggeredGridLayoutManager mLayoutManager;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && Reddit.tabletUI) {
            mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && Reddit.dualPortrait){
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        }
        rv.setLayoutManager(mLayoutManager);
        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = rv.getLayoutManager().getChildCount();
                totalItemCount = rv.getLayoutManager().getItemCount();

                int[] firstVisibleItems = null;
                firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    pastVisiblesItems = firstVisibleItems[0];
                    if (Reddit.scrollSeen) {
                        if (pastVisiblesItems > 0) {
                            HasSeen.addSeen(posts.posts.get(pastVisiblesItems - 1).getFullName());
                        }
                    }
                }

                if (!posts.loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        posts.loading = true;
                        posts.loadMore(mSwipeRefreshLayout.getContext(), SingleView.this, false, subreddit);
                    }
                }
            }
        });
        TypedValue typed_value = new TypedValue();
        getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(subreddit, this));

        mSwipeRefreshLayout.setRefreshing(true);
        posts = new SubredditPosts(subreddit);
        adapter = new SubmissionAdapter(this, posts, rv, subreddit);
        rv.setAdapter(adapter);

        posts.loadMore(mSwipeRefreshLayout.getContext(), SingleView.this, true);

        //TODO catch errors
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        posts.loadMore(mSwipeRefreshLayout.getContext(), SingleView.this, true, subreddit);
                        //TODO catch errors
                    }
                }
        );
    }

    @Override
    public void updateSuccess(final List<Submission> submissions, final int startIndex) {
        (SubmissionAdapter.sContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (startIndex != -1) {
                    adapter.notifyItemRangeInserted(startIndex, posts.posts.size());
                } else {
                    adapter.notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    public void updateOffline(List<Submission> submissions, final long cacheTime) {
        (SubmissionAdapter.sContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateOfflineError() {
        //mSwipeRefreshLayout.setRefreshing(false);
        adapter.setError(true);
    }

    @Override
    public void updateError() {
        //mSwipeRefreshLayout.setRefreshing(false);
        adapter.setError(true);
    }

}