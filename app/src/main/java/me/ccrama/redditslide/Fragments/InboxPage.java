package me.ccrama.redditslide.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Adapters.InboxAdapter;
import me.ccrama.redditslide.Adapters.InboxMessages;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Pallete;

public class InboxPage extends Fragment {


    private View v;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_verticalcontent, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
            final PreCachingLayoutManager mLayoutManager;
            mLayoutManager = new PreCachingLayoutManager(getActivity());
            rv.setLayoutManager(mLayoutManager);


        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        mSwipeRefreshLayout.setColorSchemeColors(Pallete.getColors(id, getActivity()));

        mSwipeRefreshLayout.setRefreshing(true);
        posts = new InboxMessages(id);
        adapter = new InboxAdapter(getContext(), posts, rv );
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
                            posts.loadMore(adapter, id);

                        //TODO catch errors
                    }
                }
        );
        return v;
    }

    private InboxAdapter adapter;

    private InboxMessages posts;

    private String id;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getString("id", "");
    }


}