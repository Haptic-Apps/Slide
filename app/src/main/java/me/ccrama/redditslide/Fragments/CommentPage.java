package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.CommentSearch;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.GifView;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Adapters.CommentAdapter;
import me.ccrama.redditslide.Adapters.CommentItem;
import me.ccrama.redditslide.Adapters.CommentObject;
import me.ccrama.redditslide.Adapters.SubmissionComments;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Fragment which displays comment trees.
 *
 * @see CommentsScreen
 */
public class CommentPage extends Fragment {

    boolean np;
    public boolean archived;
    public boolean locked;
    boolean loadMore;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public RecyclerView rv;
    private int page;
    private SubmissionComments comments;
    private boolean single;
    public CommentAdapter adapter;
    private String fullname;
    private String baseSubreddit;
    private String context;
    private int subredditStyle;
    private ContextWrapper contextThemeWrapper;
    private PreCachingLayoutManagerComments mLayoutManager;
    public String subreddit;
    public boolean loaded = false;


    public void doResult(Intent data) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 423 && resultCode == getActivity().RESULT_OK) {
            doResult(data);
        } else if (requestCode == 3333) {
            LogUtil.v("GEtting intent!");
            for (Fragment fragment : getFragmentManager().getFragments()) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    RecyclerView.OnScrollListener toolbarScroll;
    public Toolbar toolbar;
    public int headerHeight;
    int toSubtract;

    public void doTopBar(Submission s) {
        archived = s.isArchived();
        locked = s.isLocked();
        doTopBar();
    }

    public void doTopBar() {
        final View subtractHeight = v.findViewById(R.id.locked);
        toSubtract = 4;
        final View header = v.findViewById(R.id.header);
        v.findViewById(R.id.np).setVisibility(View.VISIBLE);
        v.findViewById(R.id.archived).setVisibility(View.VISIBLE);
        v.findViewById(R.id.locked).setVisibility(View.VISIBLE);
        v.findViewById(R.id.loadall).setVisibility(View.VISIBLE);

        if (!loadMore) {
            v.findViewById(R.id.loadall).setVisibility(View.GONE);
        } else {
            toSubtract--;
            v.findViewById(R.id.loadall).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSwipeRefreshLayout.setRefreshing(true);

                    toSubtract++;
                    headerHeight = header.getMeasuredHeight() - (subtractHeight.getHeight() * toSubtract);
                    if (adapter != null)
                        adapter.notifyItemChanged(0);
                    //avoid crashes when load more is clicked before loading is finished
                    if (comments.mLoadData != null) comments.mLoadData.cancel(true);

                    comments = new SubmissionComments(fullname, CommentPage.this, mSwipeRefreshLayout);
                    comments.setSorting(CommentSort.CONFIDENCE);
                    loadMore = false;
                    v.findViewById(R.id.loadall).setVisibility(View.GONE);

                }
            });

        }
        if (!np && !archived) {
            v.findViewById(R.id.np).setVisibility(View.GONE);
            v.findViewById(R.id.archived).setVisibility(View.GONE);
        } else if (archived) {
            toSubtract--;
            v.findViewById(R.id.np).setVisibility(View.GONE);
            v.findViewById(R.id.archived).setBackgroundColor(Palette.getColor(subreddit));
        } else {
            toSubtract--;
            v.findViewById(R.id.archived).setVisibility(View.GONE);
            v.findViewById(R.id.np).setBackgroundColor(Palette.getColor(subreddit));
        }

        if (locked) {
            toSubtract--;
        } else {
            v.findViewById(R.id.locked).setVisibility(View.GONE);
        }

        subtractHeight.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        header.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        headerHeight = header.getMeasuredHeight() - (subtractHeight.getHeight() * toSubtract);

        //If the "No participation" header was present, the offset has already been set
        if (!np) {
            //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
            //So, we just do 7% of the device screen height as a general estimate for just a toolbar.
            //Don't use "headerHeight" for consistency
            int screenHeight = this.getResources().getDisplayMetrics().heightPixels;
            int headerOffset = Math.round((float) (screenHeight * 0.07));

            //If the header has the "Load full thread", "Archived", or "Locked" header,
            //account for the extra height
            if (loadMore || archived || locked) {
                headerOffset = Math.round((float) (screenHeight * 0.11));
            }

            mSwipeRefreshLayout.setProgressViewOffset(false,
                    headerOffset - Reddit.pxToDp(42, getContext()),
                    headerOffset + Reddit.pxToDp(42, getContext()));
        }

    }

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        v = localInflater.inflate(R.layout.fragment_verticalcontenttoolbar, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        rv.setLayoutManager(mLayoutManager);
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);

        toolbarScroll = new ToolbarScrollHideHandler(toolbar, v.findViewById(R.id.header));

        rv.addOnScrollListener(toolbarScroll);
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


        toolbar.setBackgroundColor(Palette.getColor(subreddit));


        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(subreddit, getActivity()));

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we just do 7% of the device screen height as a general estimate for just a toolbar.
        //Don't use "headerHeight" for consistency
        int screenHeight = this.getResources().getDisplayMetrics().heightPixels;
        int headerOffset = Math.round((float) (screenHeight * 0.07));

        //If the header has the "Load full thread" & "No participation" header, account for the extra height
        if (np && loadMore) {
            headerOffset = Math.round((float) (screenHeight * 0.15));
        } else if (np) { //If the header has the "No participation" header, account for the extra height
            headerOffset = Math.round((float) (screenHeight * 0.11));
        }

        mSwipeRefreshLayout.setProgressViewOffset(false,
                headerOffset - Reddit.pxToDp(42, getContext()),
                headerOffset + Reddit.pxToDp(42, getContext()));


        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (comments != null) {
                            comments.loadMore(adapter, subreddit, true);
                        } else {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }

                        //TODO catch errors
                    }
                }
        );
        toolbar.setTitle(subreddit);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.menu_comment_items);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.search: {
                        if (comments.comments != null) {
                            DataShare.sharedComments = comments.comments;
                            DataShare.subAuthor = comments.submission.getAuthor();
                            Intent i = new Intent(getActivity(), CommentSearch.class);
                            if (getActivity() instanceof MainActivity)
                                getActivity().startActivityForResult(i, 423);
                            else
                                startActivityForResult(i, 423);
                        }
                    }
                    return true;
                    case R.id.sort: {
                        openPopup(toolbar);
                        return true;
                    }
                    case R.id.content: {
                        if (adapter.submission != null)
                            if (!PostMatch.openExternal(adapter.submission.getUrl())) {

                                switch (ContentType.getImageType(adapter.submission)) {
                                    case VID_ME:
                                    case STREAMABLE:
                                        if (SettingValues.video) {
                                            Intent myIntent = new Intent(getActivity(), GifView.class);

                                            myIntent.putExtra(GifView.EXTRA_STREAMABLE, adapter.submission.getUrl());
                                            getActivity().startActivity(myIntent);

                                        } else {
                                            Reddit.defaultShare(adapter.submission.getUrl(), getActivity());
                                        }
                                        break;
                                    case NSFW_IMAGE:
                                        PopulateSubmissionViewHolder.openImage(getActivity(), adapter.submission);
                                        break;
                                    case IMGUR:
                                        Intent i2 = new Intent(getActivity(), MediaView.class);
                                        if (adapter.submission.getDataNode().has("preview") && adapter.submission.getDataNode().get("preview").get("images").get(0).get("source").has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                                            String previewUrl = adapter.submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                                            i2.putExtra(MediaView.EXTRA_DISPLAY_URL, previewUrl);
                                        }
                                        i2.putExtra(MediaView.EXTRA_URL, adapter.submission.getUrl());
                                        getActivity().startActivity(i2);
                                        break;
                                    case EMBEDDED:
                                        if (SettingValues.video) {
                                            String data = adapter.submission.getDataNode().get("media_embed").get("content").asText();
                                            {
                                                Intent i = new Intent(getActivity(), FullscreenVideo.class);
                                                i.putExtra(FullscreenVideo.EXTRA_HTML, data);
                                                getActivity().startActivity(i);
                                            }
                                        } else {
                                            Reddit.defaultShare(adapter.submission.getUrl(), getActivity());
                                        }
                                        break;
                                    case NSFW_GIF:
                                        PopulateSubmissionViewHolder.openGif(false, getActivity(), adapter.submission);
                                        break;
                                    case NSFW_GFY:
                                        PopulateSubmissionViewHolder.openGif(true, getActivity(), adapter.submission);
                                        break;
                                    case REDDIT:
                                        PopulateSubmissionViewHolder.openRedditContent(adapter.submission.getUrl(), getActivity());
                                        break;
                                    case LINK:
                                    case IMAGE_LINK:
                                    case NSFW_LINK:
                                        CustomTabUtil.openUrl(adapter.submission.getUrl(), Palette.getColor(adapter.submission.getSubredditName()), getActivity());
                                        break;
                                    case NONE:
                                    case SELF:
                                        if (adapter.submission.getSelftext().isEmpty()) {
                                            Snackbar.make(rv, "Submission has no content", Snackbar.LENGTH_SHORT).show();
                                        } else {
                                            LayoutInflater inflater = getActivity().getLayoutInflater();
                                            final View dialoglayout = inflater.inflate(R.layout.parent_comment_dialog, null);
                                            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
                                            adapter.setViews(adapter.submission.getDataNode().get("selftext_html").asText(), adapter.submission.getSubredditName(), (SpoilerRobotoTextView) dialoglayout.findViewById(R.id.firstTextView), (CommentOverflow) dialoglayout.findViewById(R.id.commentOverflow));
                                            builder.setView(dialoglayout);
                                            builder.show();
                                        }
                                        break;
                                    case GFY:
                                        PopulateSubmissionViewHolder.openGif(true, getActivity(), adapter.submission);
                                        break;
                                    case ALBUM:
                                        if (SettingValues.album) {
                                            if (SettingValues.albumSwipe) {
                                                Intent i = new Intent(getActivity(), AlbumPager.class);
                                                i.putExtra(Album.EXTRA_URL, adapter.submission.getUrl());
                                                getActivity().startActivity(i);
                                                getActivity().overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                                            } else {
                                                Intent i = new Intent(getActivity(), Album.class);
                                                i.putExtra(Album.EXTRA_URL, adapter.submission.getUrl());
                                                getActivity().startActivity(i);
                                                getActivity().overridePendingTransition(R.anim.slideright, R.anim.fade_out);
                                            }
                                        } else {
                                            Reddit.defaultShare(adapter.submission.getUrl(), getActivity());

                                        }
                                        break;
                                    case IMAGE:
                                        PopulateSubmissionViewHolder.openImage(getActivity(), adapter.submission);
                                        break;
                                    case GIF:
                                        PopulateSubmissionViewHolder.openGif(false, getActivity(), adapter.submission);
                                        break;
                                    case NONE_GFY:
                                        PopulateSubmissionViewHolder.openGif(true, getActivity(), adapter.submission);
                                        break;
                                    case NONE_GIF:
                                        PopulateSubmissionViewHolder.openGif(false, getActivity(), adapter.submission);
                                        break;
                                    case NONE_IMAGE:
                                        PopulateSubmissionViewHolder.openImage(getActivity(), adapter.submission);
                                        break;
                                    case NONE_URL:
                                        CustomTabUtil.openUrl(adapter.submission.getUrl(), Palette.getColor(adapter.submission.getSubredditName()), getActivity());
                                        break;
                                    case VIDEO:
                                        Reddit.defaultShare(adapter.submission.getUrl(), getActivity());

                                }
                            } else {
                                Reddit.defaultShare(adapter.submission.getUrl(), getActivity());
                            }
                    }
                    return true;
                    case R.id.reload:
                        if (comments != null) {
                            mSwipeRefreshLayout.setRefreshing(true);
                            comments.loadMore(adapter, subreddit);
                        }
                        return true;
                    case R.id.collapse: {
                        if (adapter != null) {
                            adapter.collapseAll();
                        }
                    }
                    return true;
                    case android.R.id.home:
                        getActivity().onBackPressed();
                        return true;

                }
                return false;
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LinearLayoutManager) rv.getLayoutManager()).scrollToPositionWithOffset(1, headerHeight);
            }
        });

        doTopBar();

        if (!(getActivity() instanceof CommentsScreen) || ((CommentsScreen) getActivity()).currentPage == page) {
            doAdapter();
        }
        return v;
    }

    public void doAdapter() {

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (adapter == null || adapter.users == null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        loaded = true;
        if (!single && getActivity() instanceof CommentsScreen && ((CommentsScreen) getActivity()).subredditPosts != null) {
            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            comments.setSorting(Reddit.defaultCommentSorting);
            adapter = new CommentAdapter(this, comments, rv, ((CommentsScreen) getActivity()).subredditPosts.getPosts().get(page), getFragmentManager());
            rv.setAdapter(adapter);
        } else if (getActivity() instanceof MainActivity) {
            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            comments.setSorting(Reddit.defaultCommentSorting);
            adapter = new CommentAdapter(this, comments, rv, ((MainActivity) getActivity()).openingComments, getFragmentManager());
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
                adapter.reset(getContext(), comments, rv, (getActivity() instanceof MainActivity) ? ((MainActivity) getActivity()).openingComments : comments.submission);
                if (SettingValues.collapseCommentsDefault) {
                    adapter.collapseAll();
                }
            } catch (Exception ignored) {
            }

        } else {
            adapter.reset(getContext(), comments, rv, comments.submission);
            if (SettingValues.collapseCommentsDefault) {
                adapter.collapseAll();
            }
            adapter.notifyItemChanged(1);
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
        locked = bundle.getBoolean("locked", false);
        baseSubreddit = bundle.getString("baseSubreddit", "");

        loadMore = (!context.isEmpty() && !context.equals(Reddit.EMPTY_STRING));
        if (!single) loadMore = false;
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

    public void doGoUp(int old) {
        int pos = (old < 2) ? 0  :old-1;

        for (int i = pos - 1; i >= 0; i--) {
            CommentObject o = adapter.users.get(adapter.getRealPosition(i));
            if (o instanceof CommentItem && pos - 1 != i) {
                if (o.comment.isTopLevel()) {
                    (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, ((View) toolbar.getParent()).getTranslationY() != 0 ? 0 : toolbar.getHeight());
                    break;
                }
            }
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
                                doGoUp(mLayoutManager.findFirstCompletelyVisibleItemPosition());
                            }
                        }).setNegativeButton("No", null)
                        .show();

            } else {
                doGoUp(mLayoutManager.findFirstCompletelyVisibleItemPosition());
            }
        }
    }

    public void doGoDown(int old) {
        int pos = (old < 2) ? 0 : old-1;

        for (int i = pos + 1; i < adapter.users.size(); i++) {
            CommentObject o = adapter.users.get(adapter.getRealPosition(i));
            if (o instanceof CommentItem) {
                if (o.comment.isTopLevel()) {
                    (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, ((View) toolbar.getParent()).getTranslationY() != 0 ? 0 : toolbar.getHeight());
                    break;
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
                                doGoDown(mLayoutManager.findFirstCompletelyVisibleItemPosition());
                            }
                        }).setNegativeButton("No", null)
                        .show();

            } else {
                doGoDown(mLayoutManager.findFirstCompletelyVisibleItemPosition());
            }
        }
    }
}