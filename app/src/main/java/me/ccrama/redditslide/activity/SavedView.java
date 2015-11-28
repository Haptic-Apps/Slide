package me.ccrama.redditslide.activity;

import android.app.ActivityManager;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Window;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.adapter.ContributionAdapter;
import me.ccrama.redditslide.adapter.ContributionPosts;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.view.PreCachingLayoutManager;
import me.ccrama.redditslide.visual.FontPreferences;
import me.ccrama.redditslide.visual.Pallete;

public class SavedView extends BaseActivity {


    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private ContributionAdapter adapter;
    private String where;
    private ContributionPosts posts;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        final String id = getIntent().getExtras().getString("id", "");

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        setContentView(R.layout.activity_saved);
        where = getIntent().getExtras().getString("where", "Saved");

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setBackgroundColor(Pallete.getColorUser(id));
        setSupportActionBar(t);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getColorUser(id)));
            SavedView.this.setTaskDescription(new ActivityManager.TaskDescription(where + " posts", ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColorUser(id)));

        }

        getSupportActionBar().setTitle(where);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        mSwipeRefreshLayout.setColorSchemeColors(Pallete.getColors(id, this));

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