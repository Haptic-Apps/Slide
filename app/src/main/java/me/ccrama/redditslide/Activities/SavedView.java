package me.ccrama.redditslide.Activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Adapters.ContributionAdapter;
import me.ccrama.redditslide.Adapters.ContributionPosts;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;

public class SavedView extends BaseActivityAnim {


    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private ContributionAdapter adapter;
    private String where;
    private ContributionPosts posts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme("");
        setContentView(R.layout.activity_saved);
        where = getIntent().getExtras().getString("where", "Saved");
        final String id = getIntent().getExtras().getString("id", "");
        setupUserAppBar(R.id.toolbar, where, true, id);

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
                        posts.loadMore(adapter, id, where);

                    }
                }
            }
        });
        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(id, this));

        mSwipeRefreshLayout.setRefreshing(true);
        posts = new ContributionPosts(id, where.toLowerCase());
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
                        posts.loadMore(adapter, id, where);

                        //TODO catch errors
                    }
                }
        );
    }


}