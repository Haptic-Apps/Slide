package me.ccrama.redditslide.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Adapters.ModeratorAdapter;
import me.ccrama.redditslide.Adapters.ModeratorPosts;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;

public class ModPage extends Fragment {


    public ModeratorAdapter adapter;
    private ModeratorPosts posts;
    private String id;
    private String sub;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_verticalcontent, container, false);

        RecyclerView rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final PreCachingLayoutManager mLayoutManager;
        mLayoutManager = new PreCachingLayoutManager(getActivity());
        rv.setLayoutManager(mLayoutManager);

        v.findViewById(R.id.post_floating_action_button).setVisibility(View.GONE);

        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(id, getActivity()));

        mSwipeRefreshLayout.setProgressViewOffset(false, Reddit.pxToDp(56, getContext()), Reddit.pxToDp(92, getContext()));

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        posts = new ModeratorPosts(id, sub);
        adapter = new ModeratorAdapter(getActivity(), posts, rv);
        rv.setAdapter(adapter);

        rv.setOnScrollListener(new ToolbarScrollHideHandler(((ModQueue)getActivity()).mToolbar, (getActivity()).findViewById(R.id.header)));

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
                        posts.loadMore(adapter, id, sub);

                        //TODO catch errors
                    }
                }
        );
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getString("id", "");
        sub = bundle.getString("subreddit", "");

    }


}