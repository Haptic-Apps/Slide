package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.content.ContextWrapper;
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
import android.view.KeyEvent;
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
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;

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
    public RecyclerView rv;
    private int page;
    private SubmissionComments comments;
    private boolean single;
    private CommentAdapter adapter;
    private String fullname;
    private String baseSubreddit;
    private String context;
    private int subredditStyle;
    private ContextWrapper contextThemeWrapper;
    private PreCachingLayoutManagerComments mLayoutManager;
    public String subreddit;
    public boolean loaded = false;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            if (data.hasExtra("fullname")) {
                String fullname = data.getExtras().getString("fullname");

                adapter.currentSelectedItem = fullname;
                adapter.reset(getContext(), comments, rv, comments.submission);
                adapter.notifyDataSetChanged();
                int i = 2;
                for (CommentObject n : comments.comments) {
                    if (n instanceof CommentItem && n.comment.getComment().getFullName().contains(fullname)) {
                        ((PreCachingLayoutManagerComments) rv.getLayoutManager()).scrollToPositionWithOffset(i, toolbar.getHeight());
                        break;
                    }
                    i++;
                }

            }
        }

    }

    RecyclerView.OnScrollListener toolbarScroll;
    Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        final View v = localInflater.inflate(R.layout.fragment_verticalcontenttoolbar, container, false);
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
                    comments.setSorting(CommentSort.CONFIDENCE);
                    loadMore = false;
                    v.findViewById(R.id.loadall).setVisibility(View.GONE);

                }
            });

        }
        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        rv.setLayoutManager(mLayoutManager);
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbarScroll = new ToolbarScrollHideHandler(toolbar, v.findViewById(R.id.header));
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
        //  rv.addOnScrollListener(toolbarScroll);
        if (!SettingValues.fastscroll) {
            v.findViewById(R.id.fastscroll).setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context == null || context.isEmpty())
                        goDown();
                }
            });
            v.findViewById(R.id.up).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context == null || context.isEmpty())
                        goUp();
                }
            });
        }

        v.findViewById(R.id.up).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Scroll to top
                rv.getLayoutManager().scrollToPosition(1);
                return true;
            }
        });

        if (getActivity() instanceof BaseActivityAnim) {
            ((BaseActivityAnim) getActivity()).setSupportActionBar(toolbar);
            ((BaseActivityAnim) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((BaseActivityAnim) getActivity()).getSupportActionBar().setTitle(subreddit);
        }
        toolbar.setBackgroundColor(Palette.getColor(subreddit));

        v.findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    openPopup(v);
                }
            }
        });


        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(subreddit, getActivity()));

        mSwipeRefreshLayout.setProgressViewOffset(false, Reddit.pxToDp(56, getContext()), Reddit.pxToDp(92, getContext()));

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (adapter == null || adapter.users == null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        if (!(getActivity() instanceof CommentsScreen) || ((CommentsScreen) getActivity()).currentPage == page) {
            doAdapter();
        }
        if (!np && !archived) {
            v.findViewById(R.id.np).setVisibility(View.GONE);
            v.findViewById(R.id.archived).setVisibility(View.GONE);
        } else if (archived) {
            v.findViewById(R.id.np).setVisibility(View.GONE);
            v.findViewById(R.id.archived).setBackgroundColor(Palette.getColor(subreddit));

        } else {
            v.findViewById(R.id.archived).setVisibility(View.GONE);
            v.findViewById(R.id.np).setBackgroundColor(Palette.getColor(subreddit));
        }

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        comments.loadMore(adapter, subreddit);

                        //TODO catch errors
                    }
                }
        );


        return v;
    }

    public void doAdapter() {
        loaded = true;
        if (!single && getActivity() instanceof CommentsScreen && ((CommentsScreen) getActivity()).subredditPosts != null) {

            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            comments.setSorting(Reddit.defaultCommentSorting);
            adapter = new CommentAdapter(this, comments, rv, ((CommentsScreen) getActivity()).subredditPosts.getPosts().get(page), getFragmentManager());
            rv.setAdapter(adapter);

        } else {
            OfflineSubreddit o = OfflineSubreddit.getSubreddit(baseSubreddit);
            if (o != null && o.submissions.size() > 0 && o.submissions.size() > page && o.submissions.get(page).getComments() != null) {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, o.submissions.get(page));
                if (o.submissions.size() > 0)
                    adapter = new CommentAdapter(this, comments, rv, o.submissions.get(page), getFragmentManager());
                rv.setAdapter(adapter);
            } else if (context.isEmpty()) {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
                comments.setSorting(Reddit.defaultCommentSorting);
                if (o != null && o.submissions.size() > 0)
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
        }
    }

    public void doData(Boolean b) {
        if (adapter == null || single) {
            adapter = new CommentAdapter(this, comments, rv, comments.submission, getFragmentManager());


            rv.setAdapter(adapter);
            adapter.currentSelectedItem = context;

            adapter.reset(getContext(), comments, rv, comments.submission);


        } else if (!b) {
            try {
                adapter.reset(getContext(), comments, rv, OfflineSubreddit.getSubreddit(baseSubreddit).submissions.get(page));
            } catch (Exception ignored) {
            }
        } else {
            adapter.reset(getContext(), comments, rv, OfflineSubreddit.getSubreddit(baseSubreddit).submissions.get(page));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        subreddit = bundle.getString("subreddit", "");
        fullname = bundle.getString("id", "");
        page = bundle.getInt("page", 0);
        single = bundle.getBoolean("single", false);
        context = bundle.getString("context", "");
        np = bundle.getBoolean("np", false);
        archived = bundle.getBoolean("archived", false);
        baseSubreddit = bundle.getString("baseSubreddit", "");

        loadMore = (!context.isEmpty() && !context.equals(Reddit.EMPTY_STRING));
        subredditStyle = new ColorPreferences(getActivity()).getThemeSubreddit(subreddit);
        contextThemeWrapper = new ContextThemeWrapper(getActivity(), subredditStyle);
        mLayoutManager = new PreCachingLayoutManagerComments(getActivity());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (comments != null)
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

    public boolean onKeyDown(int keyCode) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            goDown();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            goUp();
            return true;
        }
        return false;
    }

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

    private void goUp() {
        if (adapter.users != null && adapter.users.size() > 0) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText().toString().isEmpty()) {
                new AlertDialogWrapper.Builder(getActivity())
                        .setTitle("Discard comment?")
                        .setMessage("Do you really want to discard your comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.currentlyEditing = null;
                                int pastVisiblesItems = ((LinearLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

                                for (int i = pastVisiblesItems - 2; i >= 0; i--) {
                                    if (adapter.users.get(adapter.getRealPosition(i)) instanceof CommentItem)

                                        if (adapter.users.get(adapter.getRealPosition(i)).comment.isTopLevel()) {
                                            (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, toolbar.getHeight());
                                            rv.removeOnScrollListener(toolbarScroll);
                                            rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
                                                @Override
                                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                                        rv.setOnScrollListener(toolbarScroll);
                                                    }
                                                }
                                            });

                                            break;

                                        }
                                }
                            }
                        }).setNegativeButton("No", null)
                        .show();

            } else {
                int pastVisiblesItems = ((LinearLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

                for (int i = pastVisiblesItems - 2; i >= 0; i--) {
                    if (adapter.users.get(adapter.getRealPosition(i)) instanceof CommentItem)

                        if (adapter.users.get(adapter.getRealPosition(i)).comment.isTopLevel()) {
                            (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, toolbar.getHeight());
                            rv.removeOnScrollListener(toolbarScroll);
                            rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                        rv.setOnScrollListener(toolbarScroll);
                                    }
                                }
                            });

                            break;

                        }
                }
            }
        }
    }

    private void goDown() {
        if (adapter.users != null && adapter.users.size() > 0) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText().toString().isEmpty()) {
                new AlertDialogWrapper.Builder(getActivity())
                        .setTitle("Discard comment?")
                        .setMessage("Do you really want to discard your comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.currentlyEditing = null;
                                int pastVisiblesItems = ((LinearLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

                                for (int i = pastVisiblesItems; i + 1 < adapter.getItemCount(); i++) {

                                    if (adapter.users.get(adapter.getRealPosition(i)) instanceof CommentItem)
                                        if (adapter.users.get(adapter.getRealPosition(i)).comment.isTopLevel()) {
                                            (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, toolbar.getHeight());
                                            break;
                                        }
                                }
                            }
                        }).setNegativeButton("No", null)
                        .show();

            } else {
                int pastVisiblesItems = ((LinearLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

                for (int i = pastVisiblesItems; i + 1 < adapter.getItemCount(); i++) {

                    if (adapter.users.get(adapter.getRealPosition(i)) instanceof CommentItem)
                        if (adapter.users.get(adapter.getRealPosition(i)).comment.isTopLevel()) {
                            (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, toolbar.getHeight());
                            break;
                        }
                }
            }
        }
    }


}