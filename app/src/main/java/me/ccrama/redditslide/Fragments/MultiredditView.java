package me.ccrama.redditslide.Fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.Adapters.MultiredditAdapter;
import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.SubtleSlideInUp;
import me.ccrama.redditslide.Visuals.Palette;

public class MultiredditView extends Fragment implements SubmissionDisplay {

    private MultiredditAdapter adapter;
    private MultiredditPosts posts;
    private SwipeRefreshLayout refreshLayout;
    private int id;
    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_verticalcontent, container, false);

        final RecyclerView rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final RecyclerView.LayoutManager mLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && Reddit.tabletUI) {
            mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && Reddit.dualPortrait){
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mLayoutManager = new PreCachingLayoutManager(getActivity());

        }
        rv.setLayoutManager(mLayoutManager);
        rv.setItemViewCacheSize(2);

        v.findViewById(R.id.post_floating_action_button).setVisibility(View.GONE);

        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        refreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        refreshLayout.setColorSchemeColors(Palette.getColors(SubredditStorage.multireddits.get(id).getDisplayName(), getActivity()));

        refreshLayout.setRefreshing(true);
        posts = new MultiredditPosts(SubredditStorage.multireddits.get(id).getDisplayName());
        adapter = new MultiredditAdapter(getActivity(), posts, rv, refreshLayout);
        rv.setAdapter(adapter);
        if(Reddit.animation)
            rv.setItemAnimator(new SubtleSlideInUp(getContext()));
        posts.loadMore(getActivity(), this, true, adapter);

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        posts.loadMore(getActivity(), MultiredditView.this, true, adapter);

                        //TODO catch errors
                    }
                }
        );

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
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && !posts.nomore) {
                        posts.loading = true;
                        posts.loadMore(getActivity(), MultiredditView.this, false, adapter);
                    }
                }
            }
        });
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getInt("id", 0);
    }


    @Override
    public void updateSuccess(List<Submission> submissions, int startIndex) {
        adapter.mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateOffline(List<Submission> submissions, long cacheTime) {
        adapter.setError(true);
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void updateOfflineError() {

    }

    @Override
    public void updateError() {

    }
}