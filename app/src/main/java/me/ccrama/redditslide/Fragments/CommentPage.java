package me.ccrama.redditslide.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rey.material.widget.Slider;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

import java.io.IOException;
import java.util.Calendar;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.CommentSearch;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Adapters.CommentAdapter;
import me.ccrama.redditslide.Adapters.CommentItem;
import me.ccrama.redditslide.Adapters.CommentNavType;
import me.ccrama.redditslide.Adapters.CommentObject;
import me.ccrama.redditslide.Adapters.MoreChildItem;
import me.ccrama.redditslide.Adapters.SubmissionComments;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Constants;
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
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

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
    private String context;
    private ContextWrapper contextThemeWrapper;
    private PreCachingLayoutManagerComments mLayoutManager;
    public String subreddit;
    public boolean loaded = false;
    public boolean overrideFab;


    public void doResult(Intent data) {
        if (data.hasExtra("fullname")) {
            String fullname = data.getExtras().getString("fullname");

            adapter.currentSelectedItem = fullname;
            adapter.reset(getContext(), comments, rv, comments.submission, true);
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
            for (Fragment fragment : getFragmentManager().getFragments()) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    ToolbarScrollHideHandler toolbarScroll;
    public Toolbar toolbar;
    public int headerHeight;
    public int shownHeaders = 0;

    public void doTopBar(Submission s) {
        archived = s.isArchived();
        locked = s.isLocked();
        doTopBar();
    }

    public void doTopBarNotify(Submission submission, CommentAdapter adapter2) {
        doTopBar(submission);
        if (adapter2 != null)
            adapter2.notifyItemChanged(0);
    }

    public void doRefresh(boolean b) {
        if (b) {
            v.findViewById(R.id.progress).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }

    public void doTopBar() {
        final View loadallV = v.findViewById(R.id.loadall);
        final View npV = v.findViewById(R.id.np);
        final View archivedV = v.findViewById(R.id.archived);
        final View lockedV = v.findViewById(R.id.locked);
        final View headerV = v.findViewById(R.id.toolbar);

        shownHeaders = 0;

        headerV.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        loadallV.setVisibility(View.VISIBLE);
        npV.setVisibility(View.VISIBLE);
        archivedV.setVisibility(View.VISIBLE);
        lockedV.setVisibility(View.VISIBLE);

        if (!loadMore) {
            loadallV.setVisibility(View.GONE);
        } else {
            loadallV.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            shownHeaders += loadallV.getMeasuredHeight();

            loadallV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doRefresh(true);

                    shownHeaders -= loadallV.getMeasuredHeight();
                    headerHeight = headerV.getMeasuredHeight() + shownHeaders;
                    loadallV.setVisibility(View.GONE);

                    if (adapter != null) {
                        adapter.notifyItemChanged(0);
                    }

                    //avoid crashes when load more is clicked before loading is finished
                    if (comments.mLoadData != null) {
                        comments.mLoadData.cancel(true);
                    }

                    comments = new SubmissionComments(fullname, CommentPage.this, mSwipeRefreshLayout);
                    comments.setSorting(CommentSort.CONFIDENCE);
                    loadMore = false;

                    mSwipeRefreshLayout.setProgressViewOffset(false,
                            Constants.SINGLE_HEADER_VIEW_OFFSET - Constants.PTR_OFFSET_TOP,
                            Constants.SINGLE_HEADER_VIEW_OFFSET + (Constants.PTR_OFFSET_BOTTOM + shownHeaders));
                }
            });

        }
        if (!np && !archived) {
            npV.setVisibility(View.GONE);
            archivedV.setVisibility(View.GONE);
        } else if (archived) {
            archivedV.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            shownHeaders += archivedV.getMeasuredHeight();
            npV.setVisibility(View.GONE);
            archivedV.setBackgroundColor(Palette.getColor(subreddit));
        } else {
            npV.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            shownHeaders += npV.getMeasuredHeight();
            archivedV.setVisibility(View.GONE);
            npV.setBackgroundColor(Palette.getColor(subreddit));
        }

        if (locked) {
            lockedV.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            shownHeaders += lockedV.getMeasuredHeight();
        } else {
            lockedV.setVisibility(View.GONE);
        }

        headerHeight = headerV.getMeasuredHeight() + shownHeaders;

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we estimate the height of the header in dp. Account for show headers.
        mSwipeRefreshLayout.setProgressViewOffset(false,
                Constants.SINGLE_HEADER_VIEW_OFFSET - Constants.PTR_OFFSET_TOP,
                Constants.SINGLE_HEADER_VIEW_OFFSET + (Constants.PTR_OFFSET_BOTTOM + shownHeaders));
    }

    View v;
    public View fastScroll;
    public FloatingActionButton fab;
    public int diff;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        v = localInflater.inflate(R.layout.fragment_verticalcontenttoolbar, container, false);

        rv = (RecyclerView) v.findViewById(R.id.vertical_content);
        rv.setLayoutManager(mLayoutManager);
        rv.getLayoutManager().scrollToPosition(0);

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
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();

                    final View dialoglayout = inflater.inflate(R.layout.edit_comment, null);
                    final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());

                    final EditText e = (EditText) dialoglayout.findViewById(R.id.entry);

                    //Tint the replyLine appropriately if the base theme is Light or Sepia
                    if (SettingValues.currentTheme == 1 || SettingValues.currentTheme == 5) {
                        final int TINT = ContextCompat.getColor(getContext(), R.color.md_grey_600);

                        e.setHintTextColor(TINT);
                        e.getBackground().setColorFilter(TINT, PorterDuff.Mode.SRC_IN);
                    }

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
        if (fab != null)
            fab.show();
        resetScroll(false);
        fastScroll = v.findViewById(R.id.commentnav);
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
            v.findViewById(R.id.nav).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null && adapter.users != null) {
                        int parentCount, opCount, linkCount, gildCount;
                        parentCount = 0;
                        opCount = 0;
                        linkCount = 0;
                        gildCount = 0;
                        String op = adapter.submission.getAuthor();
                        for (CommentObject o : adapter.users) {
                            if (o.comment != null && !(o instanceof MoreChildItem)) {
                                if (o.comment.isTopLevel())
                                    parentCount++;
                                if (o.comment.getComment().getTimesGilded() > 0)
                                    gildCount++;
                                if (o.comment.getComment().getAuthor() != null && o.comment.getComment().getAuthor().equals(op))
                                    opCount++;
                                if (o.comment.getComment().getDataNode().has("body_html") && o.comment.getComment().getDataNode().get("body_html").asText().contains("&lt;/a"))
                                    linkCount++;
                            }
                        }
                        new AlertDialogWrapper.Builder(getActivity())
                                .setTitle(R.string.set_nav_mode)
                                .setSingleChoiceItems(new String[]{
                                        "Parent comment (" + parentCount + ")",
                                        "OP (" + opCount + ")",
                                        "Time",
                                        "Link (" + linkCount + ")",
                                        "Gilded (" + gildCount + ")"
                                }, getCurrentSort(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                currentSort = CommentNavType.PARENTS;
                                                break;
                                            case 1:
                                                currentSort = CommentNavType.OP;
                                                break;
                                            case 2:
                                                currentSort = CommentNavType.TIME;
                                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                                final View dialoglayout = inflater.inflate(R.layout.commenttime, null);
                                                final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
                                                final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);

                                                final TextView since = (TextView) dialoglayout.findViewById(R.id.time_string);
                                                landscape.setValueRange(60, 18000, false);
                                                landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                                                    @Override
                                                    public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                                                        Calendar c = Calendar.getInstance();
                                                        sortTime = c.getTimeInMillis() - i1 * 1000;

                                                        int commentcount = 0;
                                                        for (CommentObject o : adapter.users) {
                                                            if (o.comment != null && o.comment.getComment().getDataNode().has("created") && o.comment.getComment().getCreated().getTime() > sortTime) {
                                                                commentcount += 1;
                                                            }
                                                        }
                                                        since.setText(TimeUtils.getTimeAgo(sortTime, getActivity()) + " (" + commentcount + " comments)");
                                                    }
                                                });
                                                landscape.setValue(600, false);
                                                builder.setView(dialoglayout);
                                                builder.setPositiveButton(R.string.btn_set, null).show();
                                                break;

                                            case 3:
                                                currentSort = CommentNavType.LINK;
                                                break;
                                            case 4:
                                                currentSort = CommentNavType.GILDED;
                                                break;

                                        }

                                    }
                                }).show();

                    }
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

                                switch (ContentType.getContentType(adapter.submission)) {
                                    case VID_ME:
                                    case STREAMABLE:
                                        if (SettingValues.video) {
                                            Intent myIntent = new Intent(getActivity(), MediaView.class);

                                            myIntent.putExtra(MediaView.EXTRA_URL, adapter.submission.getUrl());
                                            getActivity().startActivity(myIntent);

                                        } else {
                                            Reddit.defaultShare(adapter.submission.getUrl(), getActivity());
                                        }
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
                                    case REDDIT:
                                        PopulateSubmissionViewHolder.openRedditContent(adapter.submission.getUrl(), getActivity());
                                        break;
                                    case LINK:
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
                                        PopulateSubmissionViewHolder.openImage(getActivity(), adapter.submission, null, -1);
                                        break;
                                    case GIF:
                                        PopulateSubmissionViewHolder.openGif(getActivity(), adapter.submission, -1);
                                        break;
                                    case VIDEO:
                                        if (Reddit.videoPlugin) {
                                            try {
                                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                                sharingIntent.setClassName("ccrama.me.slideyoutubeplugin",
                                                        "ccrama.me.slideyoutubeplugin.YouTubeView");
                                                sharingIntent.putExtra("url", adapter.submission.getUrl());
                                                getActivity().startActivity(sharingIntent);

                                            } catch (Exception e) {
                                                Reddit.defaultShare(adapter.submission.getUrl(), getActivity());
                                            }
                                        } else {
                                            Reddit.defaultShare(adapter.submission.getUrl(), getActivity());
                                        }

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
                resetScroll(false);
            }
        });
        addClickFunctionSubName(toolbar);

        doTopBar();

        if (Authentication.didOnline && !NetworkUtil.isConnectedNoOverride(getActivity())) {
            new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.err_title)
                    .setMessage(R.string.err_connection_failed_msg)
                    .setNegativeButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (!(getActivity() instanceof MainActivity)) {
                                getActivity().finish();
                            }
                        }
                    }).setPositiveButton(R.string.btn_offline, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Reddit.appRestart.edit().putBoolean("forceoffline", true).apply();
                    Reddit.forceRestart(getActivity());
                }
            }).show();
        }

        if (!(getActivity() instanceof CommentsScreen) || ((CommentsScreen) getActivity()).currentPage == page) {
            doAdapter(true);
        } else {
            doAdapter(false);
        }
        return v;
    }

    public CommentSort commentSorting;

    private void addClickFunctionSubName(Toolbar toolbar) {
        TextView titleTv = null;
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            CharSequence text = null;
            if (view instanceof TextView && (text = ((TextView) view).getText()) != null) {
                titleTv = (TextView) view;
            }
        }
        if (titleTv != null) {
            final String text = titleTv.getText().toString();
            titleTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), SubredditView.class);
                    i.putExtra(SubredditView.EXTRA_SUBREDDIT, text);
                    startActivity(i);
                }
            });
        }
    }

    public void doAdapter(boolean load) {
        commentSorting = SettingValues.getCommentSorting(subreddit);
        if (load)
            doRefresh(true);
        if (load)
            loaded = true;
        if (!single && getActivity() instanceof CommentsScreen && ((CommentsScreen) getActivity()).subredditPosts != null && Authentication.didOnline) {
            comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            Submission s = ((CommentsScreen) getActivity()).currentPosts.get(page);
            if (s != null && s.getDataNode().has("suggested_sort") && !s.getDataNode().get("suggested_sort").asText().equalsIgnoreCase("null")) {
                String sorting = s.getDataNode().get("suggested_sort").asText().toUpperCase();
                sorting = sorting.replace("İ", "I");
                commentSorting = CommentSort.valueOf(sorting);
            } else if (s != null) {
                commentSorting = SettingValues.getCommentSorting(s.getSubredditName());
            }
            if (load)
                comments.setSorting(commentSorting);
            if (adapter == null) {
                adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
                rv.setAdapter(adapter);
            }
        } else if (getActivity() instanceof MainActivity) {
            if (Authentication.didOnline) {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
                Submission s = ((MainActivity) getActivity()).openingComments;
                if (s != null && s.getDataNode().has("suggested_sort") && !s.getDataNode().get("suggested_sort").asText().equalsIgnoreCase("null")) {
                    String sorting = s.getDataNode().get("suggested_sort").asText().toUpperCase();
                    sorting = sorting.replace("İ", "I");
                    commentSorting = CommentSort.valueOf(sorting);
                } else if (s != null) {
                    commentSorting = SettingValues.getCommentSorting(s.getSubredditName());
                }
                if (load)
                    comments.setSorting(commentSorting);
                if (adapter == null) {
                    adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
                    rv.setAdapter(adapter);
                }
            } else {
                Submission s = ((MainActivity) getActivity()).openingComments;
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, s);
                if (adapter == null) {
                    adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
                    rv.setAdapter(adapter);
                }
            }
        } else {
            Submission s = null;
            try {
                s = OfflineSubreddit.getSubmissionFromStorage(fullname.contains("_") ? fullname : "t3_" + fullname, getContext(), !NetworkUtil.isConnected(getActivity()), new ObjectMapper().reader());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (s != null && s.getComments() != null) {
                doRefresh(false);
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, s);
                if (adapter == null) {

                    adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
                    rv.setAdapter(adapter);
                }
            } else if (context.isEmpty()) {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
                comments.setSorting(commentSorting);
                if (adapter == null) {

                    if (s != null)
                        adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
                    rv.setAdapter(adapter);
                }
            } else {
                if (context.equals(Reddit.EMPTY_STRING)) {
                    comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
                    if (load)
                        comments.setSorting(commentSorting);
                } else {
                    comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, context);
                    if (load)
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

            adapter.reset(getContext(), comments, rv, comments.submission, b);
        } else if (!b) {
            try {
                adapter.reset(getContext(), comments, rv, (getActivity() instanceof MainActivity) ? ((MainActivity) getActivity()).openingComments : comments.submission, b);
                if (SettingValues.collapseCommentsDefault) {
                    adapter.collapseAll();
                }
            } catch (Exception ignored) {
            }

        } else {
            adapter.reset(getContext(), comments, rv, comments.submission, b);
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
        String baseSubreddit = bundle.getString("baseSubreddit", "");

        loadMore = (!context.isEmpty() && !context.equals(Reddit.EMPTY_STRING));
        if (!single) loadMore = false;
        int subredditStyle = new ColorPreferences(getActivity()).getThemeSubreddit(subreddit);
        contextThemeWrapper = new ContextThemeWrapper(getActivity(), subredditStyle);
        mLayoutManager = new PreCachingLayoutManagerComments(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (comments != null)
            comments.cancelLoad();
        if (adapter != null && adapter.users != null) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText().toString().isEmpty()) {
                Drafts.addDraft(adapter.currentlyEditing.getText().toString());
                Toast.makeText(getActivity().getApplicationContext(), R.string.msg_save_draft, Toast.LENGTH_LONG).show();
            }
        }
    }

    public int getCurrentSort() {
        switch (currentSort) {
            case PARENTS:
                return 0;
            case TIME:
                return 2;
            case GILDED:
                return 4;
            case OP:
                return 1;
            case LINK:
                return 3;
        }
        return 0;
    }

    public void resetScroll(boolean override) {
        if (toolbarScroll == null) {
            toolbarScroll = new ToolbarScrollHideHandler(toolbar, v.findViewById(R.id.header), v.findViewById(R.id.progress), SettingValues.commentAutoHide ? v.findViewById(R.id.commentnav) : null) {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (SettingValues.fabComments) {
                        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING && !overrideFab) {
                            diff += dy;
                        } else if (!overrideFab) {
                            diff = 0;
                        }
                        if (fab != null && !overrideFab) {
                            if (dy <= 0 && fab.getId() != 0) {
                                if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_DRAGGING || diff < -fab.getHeight() * 2)
                                    fab.show();
                            } else {
                                fab.hide();
                            }
                        }
                    }
                }
            };
            rv.addOnScrollListener(toolbarScroll);
        } else if(!override){
            toolbarScroll.reset = true;
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
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
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
                            break;
                        case 1:
                            commentSorting = CommentSort.TOP;
                            break;
                        case 2:
                            commentSorting = CommentSort.NEW;
                            break;
                        case 3:
                            commentSorting = CommentSort.CONTROVERSIAL;
                            break;
                        case 4:
                            commentSorting = CommentSort.OLD;
                            break;
                        case 5:
                            commentSorting = CommentSort.QA;
                            break;
                    }
                }
            };

            final int i = commentSorting == CommentSort.CONFIDENCE ? 0
                    : commentSorting == CommentSort.TOP ? 1
                    : commentSorting == CommentSort.NEW ? 2
                    : commentSorting == CommentSort.CONTROVERSIAL ? 3
                    : commentSorting == CommentSort.OLD ? 4
                    : commentSorting == CommentSort.QA ? 5
                    : 0;

            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
            builder.setTitle(R.string.sorting_choose);
            Resources res = getActivity().getBaseContext().getResources();
            builder.setSingleChoiceItems(
                    new String[]{
                            res.getString(R.string.sorting_best),
                            res.getString(R.string.sorting_top),
                            res.getString(R.string.sorting_new),
                            res.getString(R.string.sorting_controversial),
                            res.getString(R.string.sorting_old),
                            res.getString(R.string.sorting_ama)},
                    i, l2);
            builder.alwaysCallSingleChoiceCallback();
            builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reloadSubs();
                }
            }).setNeutralButton("Default for /r/" + subreddit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SettingValues.setDefaultCommentSorting(commentSorting, subreddit);
                    reloadSubs();
                }
            });
            builder.show();
        }

    }

    public void doGoUp(int old) {
        int pos = (old < 2) ? 0 : old - 1;

        for (int i = pos - 1; i >= 0; i--) {
            try {
                CommentObject o = adapter.users.get(adapter.getRealPosition(i));
                if (o instanceof CommentItem && pos - 1 != i) {
                    boolean matches = false;
                    switch (currentSort) {

                        case PARENTS:
                            matches = o.comment.isTopLevel();
                            break;
                        case TIME:
                            matches = (o.comment.getComment() != null && o.comment.getComment().getCreated().getTime() > sortTime);

                            break;
                        case GILDED:
                            matches = o.comment.getComment().getTimesGilded() > 0;
                            break;
                        case OP:
                            matches = adapter.submission != null && o.comment.getComment().getAuthor().equals(adapter.submission.getAuthor());
                            break;
                        case LINK:
                            matches = o.comment.getComment().getDataNode().get("body_html").asText().contains("&lt;/a");
                            break;
                    }
                    if (matches) {
                        if (i + 2 == old) {
                            doGoUp(old - 1);
                        } else {
                            (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, ((View) toolbar.getParent()).getTranslationY() != 0 ? 0 : (v.findViewById(R.id.header)).getHeight());
                        }
                        break;
                    }
                }
            } catch (Exception ignored) {

            }
        }
    }

    private void goUp() {
        int toGoto = mLayoutManager.findFirstVisibleItemPosition();
        if (adapter != null && adapter.users != null && !adapter.users.isEmpty()) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText().toString().isEmpty()) {
                final int finalToGoto = toGoto;
                new AlertDialogWrapper.Builder(getActivity())
                        .setTitle(R.string.discard_comment_title)
                        .setMessage(R.string.comment_discard_msg)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.currentlyEditing = null;
                                doGoUp(finalToGoto);
                            }
                        }).setNegativeButton(R.string.btn_no, null)
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
        int pos = old - 2;
        if (pos < 0) pos = 0;
        String original = adapter.users.get(adapter.getRealPosition(pos)).getName();

        for (int i = pos + 1; i < adapter.users.size(); i++) {
            try {
                CommentObject o = adapter.users.get(adapter.getRealPosition(i));
                if (o instanceof CommentItem) {
                    boolean matches = false;
                    switch (currentSort) {

                        case PARENTS:
                            matches = o.comment.isTopLevel();
                            break;
                        case TIME:
                            matches = o.comment.getComment().getCreated().getTime() > sortTime;
                            break;
                        case GILDED:
                            matches = o.comment.getComment().getTimesGilded() > 0;
                            break;
                        case OP:
                            matches = adapter.submission != null && o.comment.getComment().getAuthor().equals(adapter.submission.getAuthor());
                            break;
                        case LINK:
                            matches = o.comment.getComment().getDataNode().get("body_html").asText().contains("&lt;/a");
                            break;
                    }
                    if (matches) {
                        if (o.getName().equals(original)) {
                            doGoDown(i + 2);
                        } else {
                            (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(i + 2, ((View) toolbar.getParent()).getTranslationY() != 0 ? 0 : (v.findViewById(R.id.header).getHeight()));
                        }
                        break;
                    }
                }
            } catch (Exception ignored) {

            }
        }
    }

    private void goDown() {
        ((View) toolbar.getParent()).setTranslationY(-((View) toolbar.getParent()).getHeight());
        int toGoto = mLayoutManager.findFirstVisibleItemPosition();
        if (adapter != null && adapter.users != null && !adapter.users.isEmpty()) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText().toString().isEmpty()) {
                final int finalToGoto = toGoto;
                new AlertDialogWrapper.Builder(getActivity())
                        .setTitle(R.string.discard_comment_title)
                        .setMessage(R.string.comment_discard_msg)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.currentlyEditing = null;
                                doGoDown(finalToGoto);
                            }
                        }).setNegativeButton(R.string.btn_no, null)
                        .show();

            } else {
                doGoDown(toGoto);
            }
        }
    }

    CommentNavType currentSort = CommentNavType.PARENTS;
    long sortTime = 0;
}