package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;

import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.Adapters.CommentAdapter;
import me.ccrama.redditslide.Adapters.SubmissionComments;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Pallete;

public class CommentPage extends Fragment {

    public void reloadSubs(){
        mSwipeRefreshLayout.setRefreshing(true);
        comments.setSorting(Reddit.defaultCommentSorting);
    }
    public void openPopup(View view) {

        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        Reddit.defaultCommentSorting = CommentSort.CONFIDENCE;
                        reloadSubs();
                        break;
                    case 1:
                        Reddit.defaultCommentSorting = CommentSort.TOP;
                        reloadSubs();
                        break;
                    case 2:
                        Reddit.defaultCommentSorting = CommentSort.QA;
                        reloadSubs();
                        break;
                    case 3:
                        Reddit.defaultCommentSorting = CommentSort.NEW;
                        reloadSubs();
                        break;
                    case 4:
                        Reddit.defaultCommentSorting = CommentSort.CONTROVERSIAL;
                        reloadSubs();
                        break;
                    case 5:
                        Reddit.defaultCommentSorting = CommentSort.OLD;
                        reloadSubs();
                        break;

                }
            }
        };
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Choose a Sorting Type");
        builder.setItems(
                new String[]{"Best",
                        "Top",
                        "Q&A (AMA)",

                        "New",
                        "Controversial",
                        "Old"},
                l2);
        builder.show();

    }


    public View v;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView rv;

    int page;
    SubmissionComments comments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_verticalcontenttoolbar, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mLayoutManager);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((BaseActivity)getActivity()).setSupportActionBar(toolbar);
        ((BaseActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((BaseActivity)getActivity()).getSupportActionBar().setTitle(id);
        toolbar.setBackgroundColor(Pallete.getColor(id));


        v.findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    openPopup(v);
                }
            }
        });




        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        mSwipeRefreshLayout.setColorSchemeColors(Pallete.getColors(id));

        mSwipeRefreshLayout.setRefreshing(true);
        if(context.isEmpty()) {

            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            adapter = new CommentAdapter(getContext(), comments, rv, DataShare.sharedSubreddit.get(page));
            rv.setAdapter(adapter);

        } else {
            if(context.equals("NOTHING")){
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);

            } else {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, context);
            }


        }

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        try {
                            comments.loadMore(adapter, true, id);
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
    private class TopSnappedSmoothScroller extends LinearSmoothScroller {
        LinearLayoutManager lm;
        public TopSnappedSmoothScroller(Context context, LinearLayoutManager lm) {
            super(context);
            this.lm = lm;

        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return lm.computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;
        }
    }
    public void doData(Boolean b) {

        if(adapter == null){
            if(context != null && !context.equals("NOTHING")) {
                adapter = new CommentAdapter(getContext(), comments, rv, comments.submission);
                adapter.currentSelectedItem = context;
                int i = 1;
                for(CommentNode n : comments.comments){

                    if(n.getComment().getFullName().contains(context)){
                        RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), (LinearLayoutManager)rv.getLayoutManager());
                        smoothScroller.setTargetPosition(i);
                        (rv.getLayoutManager()).startSmoothScroll(smoothScroller);
                        break;
                    }
                    i++;
                }

            } else {
                adapter = new CommentAdapter(getContext(), comments, rv, comments.submission);

            }
            rv.setAdapter(adapter);
            adapter.reset(getContext(), comments, rv, comments.submission, 1);

        } else
        if(b == false) {
            adapter.reset(getContext(), comments, rv, DataShare.sharedSubreddit.get(page), 1);
        } else {
            adapter.reset(getContext(), comments, rv, DataShare.sharedSubreddit.get(page));

        }


    }

    public CommentAdapter adapter;

    public String fullname;
    public String id;
    public String context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getString("subreddit", "");
        fullname = bundle.getString("id", "");
        page = bundle.getInt("page", 0);
        context = bundle.getString("context", "");
    }


}