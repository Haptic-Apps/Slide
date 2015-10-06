package me.ccrama.redditslide.Fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Adapters.ContributionAdapter;
import me.ccrama.redditslide.Adapters.ContributionPosts;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Pallete;

public class ContributionsView extends Fragment {


    public View v;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView rv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_verticalcontent, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE || ! Reddit.tabletUI) {
            final LinearLayoutManager mLayoutManager;
            mLayoutManager = new LinearLayoutManager(getActivity());
            rv.setLayoutManager(mLayoutManager);
        } else {
            final StaggeredGridLayoutManager mLayoutManager;
            mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
            rv.setLayoutManager(mLayoutManager);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        mSwipeRefreshLayout.setColorSchemeColors(Pallete.getColors(id, getActivity()));

        mSwipeRefreshLayout.setRefreshing(true);
        posts = new ContributionPosts(id, where);
        adapter = new ContributionAdapter(getContext(), posts, rv );
        rv.setAdapter(adapter);

        try {
            posts.bindAdapter(adapter, mSwipeRefreshLayout);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //TODO catch errors
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        try {
                            posts.loadMore(adapter, true, id, where);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //TODO catch errors
                    }
                }
        );
        return v;
    }

    public ContributionAdapter adapter;

    public ContributionPosts posts;

    public String id;
    public String where;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getString("id", "");
        where = bundle.getString("where", "");

    }


}