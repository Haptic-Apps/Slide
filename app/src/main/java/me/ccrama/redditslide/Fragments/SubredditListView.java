package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import net.dean.jraw.models.Subreddit;

import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.Adapters.SubredditAdapter;
import me.ccrama.redditslide.Adapters.SubredditNames;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;

public class SubredditListView extends Fragment {
    public SubredditNames posts;
    public RecyclerView rv;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private int totalItemCount;
    public SubredditAdapter adapter;
    public String where;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), new ColorPreferences(inflater.getContext()).getThemeSubreddit(where));
        View v = ((LayoutInflater) contextThemeWrapper.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_verticalcontent, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final RecyclerView.LayoutManager mLayoutManager = new PreCachingLayoutManager(getActivity());

        rv.setLayoutManager(mLayoutManager);
        rv.setItemAnimator(new SlideInUpAnimator(new AccelerateDecelerateInterpolator()));

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors("no sub", getContext()));

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we just do 13% of the phone screen height as a general estimate for the Tabs view type
        final int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        final int headerOffset = Math.round((float) (screenHeight * 0.13));

        mSwipeRefreshLayout.setProgressViewOffset(false,
                headerOffset - Reddit.pxToDp(42, getContext()),
                headerOffset + Reddit.pxToDp(42, getContext()));

        v.findViewById(R.id.post_floating_action_button).setVisibility(View.GONE);

        rv.addOnScrollListener(new ToolbarScrollHideHandler(((BaseActivity) getActivity()).mToolbar, getActivity().findViewById(R.id.header)) {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!posts.loading && !posts.nomore) {

                    visibleItemCount = rv.getLayoutManager().getChildCount();
                    totalItemCount = rv.getLayoutManager().getItemCount();

                    if ((visibleItemCount + pastVisiblesItems) + 5 >= totalItemCount) {
                        posts.loading = true;
                        posts.loadMore(mSwipeRefreshLayout.getContext(), false, where);

                    }
                }

            }
        });


        doAdapter();

        return v;
    }

    public boolean main;

    public void doAdapter() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        posts = new SubredditNames(where, getContext(), SubredditListView.this);
        adapter = new SubredditAdapter(getActivity(), posts, rv, where, this);
        rv.setAdapter(adapter);
        posts.loadMore(mSwipeRefreshLayout.getContext(), true, where);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        where = bundle.getString("id", "");
    }

    private void refresh() {
        posts.loadMore(mSwipeRefreshLayout.getContext(), true, where);
    }

    public void updateSuccess(final List<Subreddit> submissions, final int startIndex) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    if (startIndex > 0) {
                        adapter.notifyItemRangeInserted(startIndex + 1, posts.posts.size());
                    } else {
                        adapter.notifyDataSetChanged();
                    }

                }
            });
        }
    }

    public void updateError() {
        mSwipeRefreshLayout.setRefreshing(false);
        adapter.setError(true);
    }
}