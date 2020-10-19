package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mikepenz.itemanimators.SlideUpAlphaAnimator;

import net.dean.jraw.models.Subreddit;

import java.util.List;

import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.Adapters.SubredditAdapter;
import me.ccrama.redditslide.Adapters.SubredditNames;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;
import me.ccrama.redditslide.util.LogUtil;

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
        View v = LayoutInflater.from(contextThemeWrapper).inflate(R.layout.fragment_verticalcontent, container, false);

        rv = v.findViewById(R.id.vertical_content);
        final RecyclerView.LayoutManager mLayoutManager = new PreCachingLayoutManager(getActivity());

        rv.setLayoutManager(mLayoutManager);
        rv.setItemAnimator(new SlideUpAlphaAnimator().withInterpolator(new LinearOutSlowInInterpolator()));

        mSwipeRefreshLayout = v.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors("no sub", getContext()));

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we estimate the height of the header in dp
        mSwipeRefreshLayout.setProgressViewOffset(false,
                Constants.TAB_HEADER_VIEW_OFFSET - Constants.PTR_OFFSET_TOP,
                Constants.TAB_HEADER_VIEW_OFFSET + Constants.PTR_OFFSET_BOTTOM);

        v.findViewById(R.id.post_floating_action_button).setVisibility(View.GONE);
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
        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);
        rv.addOnScrollListener(new ToolbarScrollHideHandler(((BaseActivity) getActivity()).mToolbar, getActivity().findViewById(R.id.header)) {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!posts.loading && !posts.nomore) {
                    visibleItemCount = rv.getLayoutManager().getChildCount();
                    totalItemCount = rv.getLayoutManager().getItemCount();

                    pastVisiblesItems = ((LinearLayoutManager)rv.getLayoutManager()).findFirstVisibleItemPosition();
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        posts.loading = true;
                        LogUtil.v("Loading more");
                        posts.loadMore(mSwipeRefreshLayout.getContext(), false, where);
                    }
                }

            }
        });
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
