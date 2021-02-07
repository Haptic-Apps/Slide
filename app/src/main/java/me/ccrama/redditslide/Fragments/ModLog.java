package me.ccrama.redditslide.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Adapters.ModLogAdapter;
import me.ccrama.redditslide.Adapters.ModLogPosts;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;

public class ModLog extends Fragment {


    public  ModLogAdapter adapter;
    private ModLogPosts   posts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_verticalcontent, container, false);

        RecyclerView rv = v.findViewById(R.id.vertical_content);
        final PreCachingLayoutManager mLayoutManager = new PreCachingLayoutManager(getActivity());
        rv.setLayoutManager(mLayoutManager);

        v.findViewById(R.id.post_floating_action_button).setVisibility(View.GONE);

        final SwipeRefreshLayout mSwipeRefreshLayout = v.findViewById(R.id.activity_main_swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors("mod", getActivity()));

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we estimate the height of the header in dp
        mSwipeRefreshLayout.setProgressViewOffset(false,
                Constants.TAB_HEADER_VIEW_OFFSET - Constants.PTR_OFFSET_TOP,
                Constants.TAB_HEADER_VIEW_OFFSET + Constants.PTR_OFFSET_BOTTOM);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        posts = new ModLogPosts();
        adapter = new ModLogAdapter(getActivity(), posts, rv);
        rv.setAdapter(adapter);

        rv.addOnScrollListener(new ToolbarScrollHideHandler(((ModQueue) getActivity()).mToolbar, (getActivity()).findViewById(R.id.header)));

        posts.bindAdapter(adapter, mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        posts.loadMore(adapter);
                    }
                }
        );
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
    }


}
