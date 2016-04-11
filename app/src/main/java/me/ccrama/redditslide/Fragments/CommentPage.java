package me.ccrama.redditslide.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Contribution;
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
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Drafts;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.DoEditorActions;
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
    public boolean overrideFab;


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
    public View fastScroll;
    public FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        v = localInflater.inflate(R.layout.fragment_verticalcontenttoolbar, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        rv.setLayoutManager(mLayoutManager);
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbar.setPopupTheme(new ColorPreferences(getActivity()).getFontStyle().getBaseId());
        if (!SettingValues.fabComments) {
            v.findViewById(R.id.comment_floating_action_button).setVisibility(View.GONE);
        } else {
            fab = (FloatingActionButton) v.findViewById(R.id.comment_floating_action_button);
            if (SettingValues.fastscroll) {
                FrameLayout.LayoutParams fabs = (FrameLayout.LayoutParams) fab.getLayoutParams();
                fabs.setMargins(fabs.leftMargin, fabs.topMargin, fabs.rightMargin, fabs.bottomMargin * 3);
                fab.setLayoutParams(fabs);
            }
            v.findViewById(R.id.comment_floating_action_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = (getActivity()).getLayoutInflater();

                    final View dialoglayout = inflater.inflate(R.layout.edit_comment, null);
                    final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());

                    final EditText e = (EditText) dialoglayout.findViewById(R.id.entry);

                    DoEditorActions.doActions(e, dialoglayout, getActivity().getSupportFragmentManager(), getActivity());

                    builder.setView(dialoglayout);
                    final Dialog d = builder.create();
                    d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    d.show();
                    dialoglayout.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.dismiss();
                        }
                    });
                    dialoglayout.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adapter.dataSet.refreshLayout.setRefreshing(true);
                            new ReplyTaskComment(adapter.submission).execute(e.getText().toString());
                            d.dismiss();
                        }

                    });
                }
            });
        }
        toolbarScroll = new ToolbarScrollHideHandler(toolbar, v.findViewById(R.id.header)) {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (fab != null && !overrideFab) {
                    if (dy <= 0 && fab.getId() != 0 && SettingValues.fabComments) {
                        fab.show();
                    } else {
                        fab.hide();

                    }
                }
            }
        };

        rv.addOnScrollListener(toolbarScroll);
        fastScroll = v.findViewById(R.id.fastscroll);
        if (!SettingValues.fastscroll) {
            fastScroll.setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goDown();
                }
            });
            v.findViewById(R.id.up).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                        if (comments.comments != null && comments.submission != null) {
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
                        if (adapter != null && adapter.submission != null)
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
                                            Snackbar s = Snackbar.make(rv, "Submission has no content", Snackbar.LENGTH_SHORT);
                                            View view = s.getView();
                                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                            tv.setTextColor(Color.WHITE);
                                            s.show();

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

    public CommentSort commentSorting;

    public void doAdapter() {
        commentSorting = SettingValues.defaultCommentSorting;

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (adapter == null || adapter.users == null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
        loaded = true;
        if (!single && getActivity() instanceof CommentsScreen && ((CommentsScreen) getActivity()).subredditPosts != null && Authentication.didOnline) {
            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            Submission s = ((CommentsScreen) getActivity()).subredditPosts.getPosts().get(page);
            if (s != null && s.getDataNode().has("suggested_sort") && !s.getDataNode().get("suggested_sort").asText().equalsIgnoreCase("null")) {
                commentSorting = CommentSort.valueOf(s.getDataNode().get("suggested_sort").asText().toUpperCase());
            }
            comments.setSorting(commentSorting);
            adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
            rv.setAdapter(adapter);
        } else if (getActivity() instanceof MainActivity && Authentication.didOnline) {
            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            Submission s = ((MainActivity) getActivity()).openingComments;
            if (s != null && s.getDataNode().has("suggested_sort") && !s.getDataNode().get("suggested_sort").asText().equalsIgnoreCase("null")) {
                commentSorting = CommentSort.valueOf(s.getDataNode().get("suggested_sort").asText().toUpperCase());
            }
            comments.setSorting(commentSorting);
            adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
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
                comments.setSorting(commentSorting);
                if (o != null && o.submissions.size() > 0)
                    adapter = new CommentAdapter(this, comments, rv, o.submissions.get(page), getFragmentManager());
                rv.setAdapter(adapter);
            } else {
                if (context.equals(Reddit.EMPTY_STRING)) {
                    comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
                    comments.setSorting(commentSorting);
                } else {
                    comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, context);
                    comments.setSorting(commentSorting);
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

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null && adapter.users != null) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText().toString().isEmpty()) {
                Drafts.addDraft(adapter.currentlyEditing.getText().toString());
                Toast.makeText(getActivity().getApplicationContext(), R.string.msg_save_draft, Toast.LENGTH_LONG).show();
            }
        }
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //This is the filter
        if (event.getAction() != KeyEvent.ACTION_DOWN)
            return true;
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
        comments.setSorting(commentSorting);
    }

    private void openPopup(View view) {
        if (comments.comments != null && !comments.comments.isEmpty()) {
            final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case 0:
                            commentSorting = CommentSort.CONFIDENCE;
                            reloadSubs();
                            break;
                        case 1:
                            commentSorting = CommentSort.TOP;
                            reloadSubs();
                            break;
                        case 2:
                            commentSorting = CommentSort.QA;
                            reloadSubs();
                            break;
                        case 3:
                            commentSorting = CommentSort.NEW;
                            reloadSubs();
                            break;
                        case 4:
                            commentSorting = CommentSort.CONTROVERSIAL;
                            reloadSubs();
                            break;
                        case 5:
                            commentSorting = CommentSort.OLD;
                            reloadSubs();
                            break;
                    }
                }
            };
            int i = commentSorting == CommentSort.CONFIDENCE ? 0
                    : commentSorting == CommentSort.TOP ? 1
                    : commentSorting == CommentSort.QA ? 2
                    : commentSorting == CommentSort.NEW ? 3
                    : commentSorting == CommentSort.CONTROVERSIAL ? 4
                    : commentSorting == CommentSort.OLD ? 5
                    : 0;
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
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
        int pos = (old < 2) ? 0 : old - 1;

        for (int i = pos - 1; i >= 0; i--) {
            CommentObject o = adapter.users.get(adapter.getRealPosition(i));
            if (o instanceof CommentItem && pos - 1 != i) {
                if (o.comment.isTopLevel()) {
                    if (i + 2 == old) {
                        doGoUp(old - 1);
                    } else {
                        (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, ((View) toolbar.getParent()).getTranslationY() != 0 ? 0 : (v.findViewById(R.id.header)).getHeight());
                    }
                    break;
                }
            }
        }
    }

    private void goUp() {
        int toGoto = mLayoutManager.findFirstVisibleItemPosition();
        if (mLayoutManager.findFirstVisibleItemPosition() != mLayoutManager.findFirstCompletelyVisibleItemPosition()) {
            toGoto = mLayoutManager.findFirstCompletelyVisibleItemPosition();
        }
        if (adapter.users != null && adapter.users.size() > 0) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText().toString().isEmpty()) {
                final int finalToGoto = toGoto;
                new AlertDialogWrapper.Builder(getActivity())
                        .setTitle("Discard comment?")
                        .setMessage("Do you really want to discard your comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.currentlyEditing = null;
                                doGoUp(finalToGoto);
                            }
                        }).setNegativeButton("No", null)
                        .show();

            } else {
                doGoUp(toGoto);
            }
        }
    }

    public class ReplyTaskComment extends AsyncTask<String, Void, String> {
        public Contribution sub;

        public ReplyTaskComment(Contribution n) {
            sub = n;
        }

        @Override
        public void onPostExecute(final String s) {
            adapter.dataSet.refreshLayout.setRefreshing(false);
            adapter.dataSet.loadMoreReplyTop(adapter, s);

        }

        @Override
        protected String doInBackground(String... comment) {
            if (Authentication.me != null) {
                try {
                    return new AccountManager(Authentication.reddit).reply(sub, comment[0]);
                } catch (ApiException e) {
                    Log.v(LogUtil.getTag(), "UH OH!!");
                    //todo this
                }
            }
            return null;
        }

    }

    public void doGoDown(int old) {
        int pos = (old < 2) ? 0 : old - 1;

        for (int i = pos; i < adapter.users.size(); i++) {
            CommentObject o = adapter.users.get(adapter.getRealPosition(i));
            if (o instanceof CommentItem) {
                if (o.comment.isTopLevel()) {
                    if (i + 2 == old) {
                        doGoDown(old + 1);
                    } else {
                        (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, ((View) toolbar.getParent()).getTranslationY() != 0 ? 0 : (v.findViewById(R.id.header).getHeight()));
                    }
                    break;
                }
            }
        }
    }

    private void goDown() {
        int toGoto = mLayoutManager.findFirstVisibleItemPosition();
        if (mLayoutManager.findFirstVisibleItemPosition() != mLayoutManager.findFirstCompletelyVisibleItemPosition()) {
            toGoto = mLayoutManager.findFirstCompletelyVisibleItemPosition();
        }
        if (adapter.users != null && adapter.users.size() > 0) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText().toString().isEmpty()) {
                final int finalToGoto = toGoto;
                new AlertDialogWrapper.Builder(getActivity())
                        .setTitle("Discard comment?")
                        .setMessage("Do you really want to discard your comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.currentlyEditing = null;
                                doGoDown(finalToGoto);
                            }
                        }).setNegativeButton("No", null)
                        .show();

            } else {

                doGoDown(toGoto);
            }
        }
    }
}