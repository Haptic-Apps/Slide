package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.CommentSort;

import me.ccrama.redditslide.Activities.BaseActivityAnim;
import me.ccrama.redditslide.Activities.CommentSearch;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Adapters.CommentAdapter;
import me.ccrama.redditslide.Adapters.CommentItem;
import me.ccrama.redditslide.Adapters.CommentObject;
import me.ccrama.redditslide.Adapters.SubmissionComments;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Fragment which displays comment trees.
 *
 * @see CommentsScreen
 */
public class CommentPage extends Fragment {

    boolean np;
    boolean archived;
    boolean loadMore;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView rv;
    private int page;
    private SubmissionComments comments;
    private boolean single;
    private CommentAdapter adapter;
    private String fullname;
    private String id;
    private String baseSubreddit;
    private String context;

    private void reloadSubs() {
        mSwipeRefreshLayout.setRefreshing(true);
        comments.setSorting(Reddit.defaultCommentSorting);
    }

    private void openPopup(View view) {
        if (comments.comments != null && !comments.comments.isEmpty()) {
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
                    : 0;
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getContext());
            builder.setTitle(R.string.sorting_choose);
            Resources res = getActivity().getBaseContext().getResources();
            builder.setSingleChoiceItems(
                    new String[]{res.getString(R.string.sorting_best),
                            res.getString(R.string.sorting_top),
                            res.getString(R.string.sorting_ama),
                            res.getString(R.string.sorting_new),
                            res.getString(R.string.sorting_controversial),
                            res.getString(R.string.sorting_old)},
                    i, l2);
            builder.show();
        }

    }
public OfflineSubreddit o;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            if (data.hasExtra("fullname")) {
                String fullname = data.getExtras().getString("fullname");

                adapter.currentSelectedItem = fullname;
                adapter.reset(getContext(), comments, rv, comments.submission);
                adapter.notifyDataSetChanged();
                int i = 1;
                for (CommentObject n : comments.comments) {

                    if(n instanceof CommentItem)

                    if (n.comment.getComment().getFullName().contains(fullname)) {
                        RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), (PreCachingLayoutManagerComments) rv.getLayoutManager());
                        smoothScroller.setTargetPosition(i);
                        (rv.getLayoutManager()).startSmoothScroll(smoothScroller);
                        break;
                    }
                    i++;
                }

            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        int style = new ColorPreferences(getActivity()).getThemeSubreddit(id);
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), style);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View v = localInflater.inflate(R.layout.fragment_verticalcontenttoolbar, container, false);


        if (!loadMore) {
            v.findViewById(R.id.loadall).setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.loadall).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSwipeRefreshLayout.setRefreshing(true);

                    //avoid crashes when load more is clicked before loading is finished
                    if (comments.mLoadData != null) comments.mLoadData.cancel(true);

                    comments = new SubmissionComments(fullname, CommentPage.this, mSwipeRefreshLayout);
                    comments.setSorting(Reddit.defaultCommentSorting);
                    loadMore = false;
                    v.findViewById(R.id.loadall).setVisibility(View.GONE);

                }
            });

        }
        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final PreCachingLayoutManagerComments mLayoutManager;
        mLayoutManager = new PreCachingLayoutManagerComments(getActivity());
        rv.setLayoutManager(mLayoutManager);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        v.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comments.comments != null) {
                    DataShare.sharedComments = comments.comments;
                    DataShare.subAuthor = comments.submission.getAuthor();
                    Intent i = new Intent(getActivity(), CommentSearch.class);
                    startActivityForResult(i, 1);
                }

            }
        });
        if (!SettingValues.fastscroll) {
            v.findViewById(R.id.fastscroll).setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter.users != null) {
                        int pastVisiblesItems = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();

                        for (int i = pastVisiblesItems; i + 1 < adapter.getItemCount(); i++) {

                            if(adapter.users.get(adapter.getRealPosition(i)) instanceof CommentItem)
                            if (adapter.users.get(adapter.getRealPosition(i)).comment.isTopLevel()) {
                                RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), (PreCachingLayoutManagerComments) rv.getLayoutManager());
                                smoothScroller.setTargetPosition(i + 1);
                                (rv.getLayoutManager()).startSmoothScroll(smoothScroller);
                                break;
                            }
                        }
                    }
                }
            });
            v.findViewById(R.id.up).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter.users != null) {

                        int pastVisiblesItems = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();

                        for (int i = pastVisiblesItems - 2; i >= 0; i--) {
                            if(adapter.users.get(adapter.getRealPosition(i)) instanceof CommentItem)

                                if (adapter.users.get(adapter.getRealPosition(i)).comment.isTopLevel()) {
                                RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), (PreCachingLayoutManagerComments) rv.getLayoutManager());
                                smoothScroller.setTargetPosition(i + 1);
                                (rv.getLayoutManager()).startSmoothScroll(smoothScroller);
                                break;
                            }
                        }
                    }
                }
            });
        }

        v.findViewById(R.id.up).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Scroll to top
                rv.getLayoutManager().scrollToPosition(0);
                return true;
            }
        });

        if (getActivity() instanceof BaseActivityAnim) {
            ((BaseActivityAnim) getActivity()).setSupportActionBar(toolbar);
            ((BaseActivityAnim) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((BaseActivityAnim) getActivity()).getSupportActionBar().setTitle(id);
        }
        toolbar.setBackgroundColor(Palette.getColor(id));


      /* STARTING IT  v.findViewById(R.id.fab).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:


                        break;

                    case MotionEvent.ACTION_UP:

                        View body = v.findViewById(R.id.body);
                        int cx = (view.getLeft() + view.getRight()) / 2;
                        int cy = (view.getTop() + view.getBottom()) / 2;

// get the final radius for the clipping circle
                        int finalRadius = body.getWidth();

// create and start the animator for this view
// (the start radius is zero)
                        SupportAnimator anim =  io.codetail.animation.ViewAnimationUtils.createCircularReveal(body, cx, cy, 0, finalRadius);
                        anim.start();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        view.setLayoutParams(params);
                        break;
                }

                return true;
            }
        });*/
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

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(id, getActivity()));

        mSwipeRefreshLayout.setRefreshing(true);

        if(((CommentsScreen)getActivity()).o != null){
           o =  ((CommentsScreen)getActivity()).o;
        } else {
            o   = new OfflineSubreddit(baseSubreddit);
        }
        if(o.submissions.size() > 0 && o.submissions.get(page).getComments() != null){
            Log.v(LogUtil.getTag(), "Loading from cached stuff");
            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, o.submissions.get(page));
            if (o.submissions.size() > 0)
                adapter = new CommentAdapter(this, comments, rv, o.submissions.get(page), getFragmentManager());
            rv.setAdapter(adapter);
        } else if (context.isEmpty()) {
            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            comments.setSorting(Reddit.defaultCommentSorting);
            if (o.submissions.size() > 0)
                adapter = new CommentAdapter(this, comments, rv, o.submissions.get(page), getFragmentManager());
            rv.setAdapter(adapter);
        } else {
            if (context.equals(Reddit.EMPTY_STRING)) {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
                comments.setSorting(Reddit.defaultCommentSorting);
            } else {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, context);
                comments.setSorting(Reddit.defaultCommentSorting);
            }


        }
        if (!np && !archived) {
            v.findViewById(R.id.np).setVisibility(View.GONE);
            v.findViewById(R.id.archived).setVisibility(View.GONE);
        } else if (archived) {
            v.findViewById(R.id.np).setVisibility(View.GONE);
            v.findViewById(R.id.archived).setBackgroundColor(Palette.getColor(id));

        } else {
            v.findViewById(R.id.archived).setVisibility(View.GONE);
            v.findViewById(R.id.np).setBackgroundColor(Palette.getColor(id));
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

    public void doData(Boolean b) {
        if (adapter == null || single) {
            if (context != null && !context.equals(Reddit.EMPTY_STRING)) {
                adapter = new CommentAdapter(this, comments, rv, comments.submission, getFragmentManager());
                if (single) {
                    adapter.currentSelectedItem = context;

                    int i = 1;
                    for (CommentObject n : comments.comments) {
                        if(n instanceof CommentItem)

                        if (n.comment.getComment().getFullName().contains(context)) {
                            RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), (PreCachingLayoutManagerComments) rv.getLayoutManager());
                            smoothScroller.setTargetPosition(i);
                            (rv.getLayoutManager()).startSmoothScroll(smoothScroller);
                            break;
                        }
                        i++;
                    }
                }
            } else {
                adapter = new CommentAdapter(this, comments, rv, comments.submission, getFragmentManager());
            }
            rv.setAdapter(adapter);
            adapter.reset(getContext(), comments, rv, comments.submission);
        } else if (!b) {
            try {
                adapter.reset(getContext(), comments, rv, new OfflineSubreddit(baseSubreddit).submissions.get(page));
            } catch(Exception ignored){}
        } else {
            adapter.reset(getContext(), comments, rv, new OfflineSubreddit(baseSubreddit).submissions.get(page));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getString("subreddit", "");
        fullname = bundle.getString("id", "");
        page = bundle.getInt("page", 0);
        single = bundle.getBoolean("single", false);
        context = bundle.getString("context", "");
        np = bundle.getBoolean("np", false);
        archived = bundle.getBoolean("archived", false);
        subreddit = bundle.getString("subreddit", "");
        baseSubreddit = bundle.getString("baseSubreddit", "");

        loadMore = (!context.isEmpty() && !context.equals(Reddit.EMPTY_STRING));
    }

    public String subreddit;
    @Override
    public void onPause() {
        super.onPause();
        comments.cancelLoad();
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


}