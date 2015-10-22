package me.ccrama.redditslide.Activities;

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

import me.ccrama.redditslide.Adapters.ContributionAdapter;
import me.ccrama.redditslide.Adapters.ContributionPosts;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;

public class SavedView extends BaseActivity {


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rv;

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

    private ContributionAdapter adapter;

    private String where;
    private ContributionPosts posts;





}