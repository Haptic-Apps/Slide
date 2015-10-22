package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;

import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.Activities.CommentSearch;
import me.ccrama.redditslide.Adapters.CommentAdapter;
import me.ccrama.redditslide.Adapters.SubmissionComments;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Visuals.Pallete;

public class CommentPage extends Fragment {

    private void reloadSubs(){
        mSwipeRefreshLayout.setRefreshing(true);
        comments.setSorting(Reddit.defaultCommentSorting);
    }
    private void openPopup(View view) {

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
                SettingValues.prefs.edit().putString("defaultCommentSorting", Reddit.defaultCommentSorting.name()).apply();
                SettingValues.defaultCommentSorting = Reddit.defaultCommentSorting;
            }
        };
        int i = Reddit.defaultCommentSorting == CommentSort.CONFIDENCE ? 0
                : Reddit.defaultCommentSorting == CommentSort.TOP ? 1
                : Reddit.defaultCommentSorting == CommentSort.QA ? 2
                : Reddit.defaultCommentSorting == CommentSort.NEW ? 3
                : Reddit.defaultCommentSorting == CommentSort.CONTROVERSIAL ? 4
                : Reddit.defaultCommentSorting == CommentSort.OLD ? 5
                : 1;
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getContext());
        builder.setTitle("Choose a Sorting Type");
        builder.setSingleChoiceItems(
                new String[]{"Best",
                        "Top",
                        "Q&A (AMA)",
                        "New",
                        "Controversial",
                        "Old"}, i,
                l2);
        builder.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == getActivity().RESULT_OK){
            if(data.hasExtra("fullname")){
                String fullname = data.getExtras().getString("fullname");

                adapter.currentSelectedItem = fullname;
                adapter.reset(getContext(), comments, rv, comments.submission);
                adapter.notifyDataSetChanged();
                int i = 1;
                for(CommentNode n : comments.comments){

                    if(n.getComment().getFullName().contains(fullname)){
                        RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), (PreCachingLayoutManagerComments)rv.getLayoutManager());
                        smoothScroller.setTargetPosition(i);
                        (rv.getLayoutManager()).startSmoothScroll(smoothScroller);
                        break;
                    }
                    i++;
                }

            }
        }

    }
    private View v;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rv;

    private int page;
    private SubmissionComments comments;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


            int style = new ColorPreferences(getActivity()).getThemeSubreddit(id);
            final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), style);
            LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
            v = localInflater.inflate(R.layout.fragment_verticalcontenttoolbar, container, false);

            rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
            final PreCachingLayoutManagerComments mLayoutManager;
            mLayoutManager = new PreCachingLayoutManagerComments(getActivity());
            rv.setLayoutManager(mLayoutManager);

            Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        v.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comments.comments != null) {
                    DataShare.sharedComments = comments.comments;
                    DataShare.subAuthor = comments.submission.getAuthor();
                    Intent i = new Intent(getActivity(), CommentSearch.class);
                    startActivityForResult(i, 1);
                }

            }
        });
            if (getActivity() instanceof BaseActivity) {
                ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
                ((BaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((BaseActivity) getActivity()).getSupportActionBar().setTitle(id);
            }
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

            mSwipeRefreshLayout.setColorSchemeColors(Pallete.getColors(id, getActivity()));

            mSwipeRefreshLayout.setRefreshing(true);
            if (context.isEmpty()) {

                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
                if(DataShare.sharedSubreddit != null)
                adapter = new CommentAdapter(getContext(), comments, rv, DataShare.sharedSubreddit.get(page), getFragmentManager());
                rv.setAdapter(adapter);

            } else {
                if (context.equals("NOTHING")) {
                    comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);

                } else {
                    comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, context);
                }


            }

            mSwipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                                comments.loadMore(adapter, id);

                            //TODO catch errors
                        }
                    }
            );

        return v;
    }
    public static class TopSnappedSmoothScroller extends LinearSmoothScroller {
        final PreCachingLayoutManagerComments lm;
        public TopSnappedSmoothScroller(Context context, PreCachingLayoutManagerComments lm) {
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

    private boolean single;
    public void doData(Boolean b) {

        if(adapter == null || single){
            if(context != null && !context.equals("NOTHING")) {
                adapter = new CommentAdapter(getContext(), comments, rv, comments.submission,getFragmentManager());
                adapter.currentSelectedItem = context;
                int i = 1;
                for(CommentNode n : comments.comments){

                    if(n.getComment().getFullName().contains(context)){
                        RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), (PreCachingLayoutManagerComments)rv.getLayoutManager());
                        smoothScroller.setTargetPosition(i);
                        (rv.getLayoutManager()).startSmoothScroll(smoothScroller);
                        break;
                    }
                    i++;
                }

            } else {
                adapter = new CommentAdapter(getContext(), comments, rv, comments.submission,getFragmentManager());

            }
            rv.setAdapter(adapter);
            adapter.reset(getContext(), comments, rv, comments.submission);

        } else
        if(!b) {
            adapter.reset(getContext(), comments, rv, DataShare.sharedSubreddit.get(page));
        } else {
            adapter.reset(getContext(), comments, rv, DataShare.sharedSubreddit.get(page));

        }


    }

    private CommentAdapter adapter;

    private String fullname;
    private String id;
    private String context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getString("subreddit", "");
        fullname = bundle.getString("id", "");
        page = bundle.getInt("page", 0);
        single = bundle.getBoolean("single", false);
        context = bundle.getString("context", "");
    }


}