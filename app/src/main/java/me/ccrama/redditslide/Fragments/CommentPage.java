package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rey.material.widget.Slider;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.MultiRedditUpdateRequest;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserRecordPaginator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.CommentSearch;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Related;
import me.ccrama.redditslide.Activities.SendMessage;
import me.ccrama.redditslide.Activities.ShadowboxComments;
import me.ccrama.redditslide.Activities.Submit;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Tumblr;
import me.ccrama.redditslide.Activities.TumblrPager;
import me.ccrama.redditslide.Activities.Wiki;
import me.ccrama.redditslide.Adapters.CommentAdapter;
import me.ccrama.redditslide.Adapters.CommentItem;
import me.ccrama.redditslide.Adapters.CommentNavType;
import me.ccrama.redditslide.Adapters.CommentObject;
import me.ccrama.redditslide.Adapters.CommentUrlObject;
import me.ccrama.redditslide.Adapters.MoreChildItem;
import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.Adapters.SubmissionComments;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Drafts;
import me.ccrama.redditslide.ImageFlairs;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SubmissionParser;

/**
 * Fragment which displays comment trees.
 *
 * @see CommentsScreen
 */
public class CommentPage extends Fragment implements Toolbar.OnMenuItemClickListener {

    boolean np;
    public boolean archived, locked, contest;
    boolean loadMore;
    private SwipeRefreshLayout              mSwipeRefreshLayout;
    public  RecyclerView                    rv;
    private int                             page;
    private SubmissionComments              comments;
    private boolean                         single;
    public  CommentAdapter                  adapter;
    private String                          fullname;
    private String                          context;
    private int                             contextNumber;
    private ContextWrapper                  contextThemeWrapper;
    private PreCachingLayoutManagerComments mLayoutManager;
    public  String                          subreddit;
    public boolean loaded = false;
    public boolean overrideFab;
    private boolean upvoted   = false;
    private boolean downvoted = false;
    private boolean currentlySubbed;
    private boolean collapsed = SettingValues.collapseCommentsDefault;


    public void doResult(Intent data) {
        if (data.hasExtra("fullname")) {
            String fullname = data.getExtras().getString("fullname");

            adapter.currentSelectedItem = fullname;
            adapter.reset(getContext(), comments, rv, comments.submission, true);
            adapter.notifyDataSetChanged();
            int i = 2;
            for (CommentObject n : comments.comments) {
                if (n instanceof CommentItem && n.comment.getComment()
                        .getFullName()
                        .contains(fullname)) {
                    ((PreCachingLayoutManagerComments) rv.getLayoutManager()).scrollToPositionWithOffset(
                            i, toolbar.getHeight());
                    break;
                }
                i++;
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 423 && resultCode == Activity.RESULT_OK) {
            doResult(data);
        } else if (requestCode == 3333) {
            for (Fragment fragment : getFragmentManager().getFragments()) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    ToolbarScrollHideHandler toolbarScroll;
    public Toolbar toolbar;
    public int     headerHeight;
    public int shownHeaders = 0;

    public void doTopBar(Submission s) {
        archived = s.isArchived();
        locked = s.isLocked();
        contest = s.getDataNode().get("contest_mode").asBoolean();
        doTopBar();
    }

    public void doTopBarNotify(Submission submission, CommentAdapter adapter2) {
        doTopBar(submission);
        if (adapter2 != null) adapter2.notifyItemChanged(0);
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
        final View contestV = v.findViewById(R.id.contest);

        shownHeaders = 0;

        headerV.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        loadallV.setVisibility(View.VISIBLE);
        npV.setVisibility(View.VISIBLE);
        archivedV.setVisibility(View.VISIBLE);
        lockedV.setVisibility(View.VISIBLE);
        contestV.setVisibility(View.VISIBLE);

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

                    comments =
                            new SubmissionComments(fullname, CommentPage.this, mSwipeRefreshLayout);
                    comments.setSorting(CommentSort.CONFIDENCE);
                    loadMore = false;

                    mSwipeRefreshLayout.setProgressViewOffset(false,
                            Constants.SINGLE_HEADER_VIEW_OFFSET - Constants.PTR_OFFSET_TOP,
                            Constants.SINGLE_HEADER_VIEW_OFFSET + (Constants.PTR_OFFSET_BOTTOM
                                    + shownHeaders));
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
            lockedV.setBackgroundColor(Palette.getColor(subreddit));
        } else {
            lockedV.setVisibility(View.GONE);
        }

        if (contest) {
            contestV.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            shownHeaders += contestV.getMeasuredHeight();
            contestV.setBackgroundColor(Palette.getColor(subreddit));
        } else {
            contestV.setVisibility(View.GONE);
        }

        headerHeight = headerV.getMeasuredHeight() + shownHeaders;

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we estimate the height of the header in dp. Account for show headers.
        mSwipeRefreshLayout.setProgressViewOffset(false,
                Constants.SINGLE_HEADER_VIEW_OFFSET - Constants.PTR_OFFSET_TOP,
                Constants.SINGLE_HEADER_VIEW_OFFSET + (Constants.PTR_OFFSET_BOTTOM + shownHeaders));
    }

    View v;
    public View                 fastScroll;
    public FloatingActionButton fab;
    public int                  diff;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        v = localInflater.inflate(R.layout.fragment_verticalcontenttoolbar, container, false);

        rv = v.findViewById(R.id.vertical_content);
        rv.setLayoutManager(mLayoutManager);
        rv.getLayoutManager().scrollToPosition(0);

        toolbar = v.findViewById(R.id.toolbar);
        toolbar.setPopupTheme(new ColorPreferences(getActivity()).getFontStyle().getBaseId());

        if (!SettingValues.fabComments || archived || np || locked) {
            v.findViewById(R.id.comment_floating_action_button).setVisibility(View.GONE);
        } else {
            fab = v.findViewById(R.id.comment_floating_action_button);
            if (SettingValues.fastscroll) {
                FrameLayout.LayoutParams fabs = (FrameLayout.LayoutParams) fab.getLayoutParams();
                fabs.setMargins(fabs.leftMargin, fabs.topMargin, fabs.rightMargin,
                        fabs.bottomMargin * 3);
                fab.setLayoutParams(fabs);
            }
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final MaterialDialog replyDialog = new MaterialDialog.Builder(getActivity())
                            .customView(R.layout.edit_comment, false)
                            .cancelable(false)
                            .build();
                    final View replyView = replyDialog.getCustomView();

                    // Make the account selector visible
                    replyView.findViewById(R.id.profile).setVisibility(View.VISIBLE);

                    final EditText e = replyView.findViewById(R.id.entry);

                    //Tint the replyLine appropriately if the base theme is Light or Sepia
                    if (SettingValues.currentTheme == 1 || SettingValues.currentTheme == 5) {
                        final int TINT = ContextCompat.getColor(getContext(), R.color.md_grey_600);

                        e.setHintTextColor(TINT);
                        e.getBackground().setColorFilter(TINT, PorterDuff.Mode.SRC_IN);
                    }

                    DoEditorActions.doActions(e, replyView,
                            getActivity().getSupportFragmentManager(), getActivity(),
                            adapter.submission.isSelfPost() ? adapter.submission.getSelftext()
                                    : null, new String[]{adapter.submission.getAuthor()});

                    replyDialog.getWindow()
                            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    replyView.findViewById(R.id.cancel)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    replyDialog.dismiss();
                                }
                            });
                    final TextView profile = replyView.findViewById(R.id.profile);
                    final String[] changedProfile = {Authentication.name};
                    profile.setText("/u/".concat(changedProfile[0]));
                    profile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final HashMap<String, String> accounts = new HashMap<>();

                            for (String s : Authentication.authentication.getStringSet("accounts",
                                    new HashSet<String>())) {
                                if (s.contains(":")) {
                                    accounts.put(s.split(":")[0], s.split(":")[1]);
                                } else {
                                    accounts.put(s, "");
                                }
                            }
                            final ArrayList<String> keys = new ArrayList<>(accounts.keySet());
                            final int i = keys.indexOf(changedProfile[0]);

                            MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
                            builder.title(getString(R.string.replies_switch_accounts));
                            builder.items(keys.toArray(new String[keys.size()]));
                            builder.itemsCallbackSingleChoice(i, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View itemView,
                                                           int which, CharSequence text) {
                                    changedProfile[0] = keys.get(which);
                                    profile.setText("/u/".concat(changedProfile[0]));
                                    return true;
                                }
                            });
                            builder.alwaysCallSingleChoiceCallback();
                            builder.negativeText(R.string.btn_cancel);
                            builder.show();
                        }
                    });
                    replyView.findViewById(R.id.submit)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    adapter.dataSet.refreshLayout.setRefreshing(true);
                                    adapter.new ReplyTaskComment(adapter.submission,
                                            changedProfile[0]).execute(
                                            e.getText().toString());
                                    replyDialog.dismiss();
                                }

                            });

                    replyDialog.show();
                }
            });
        }
        if (fab != null) fab.show();
        resetScroll(false);
        fastScroll = v.findViewById(R.id.commentnav);
        if (!SettingValues.fastscroll) {
            fastScroll.setVisibility(View.GONE);
        } else {
            if (!SettingValues.showCollapseExpand) {
                v.findViewById(R.id.collapse_expand).setVisibility(View.GONE);
            } else {
                v.findViewById(R.id.collapse_expand).setVisibility(View.VISIBLE);
                v.findViewById(R.id.collapse_expand).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adapter != null) {
                            if (collapsed) {
                                adapter.expandAll();
                                collapsed = !collapsed;
                            } else {
                                adapter.collapseAll();
                                collapsed = !collapsed;
                            }
                        }
                    }
                });
            }
            v.findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null && adapter.keys != null && adapter.keys.size() > 0) {
                        goDown();
                    }
                }
            });
            v.findViewById(R.id.up).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null && adapter.keys != null && adapter.keys.size() > 0) goUp();
                }
            });
            v.findViewById(R.id.nav).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null && adapter.currentComments != null) {
                        int parentCount, opCount, linkCount, awardCount;
                        parentCount = 0;
                        opCount = 0;
                        linkCount = 0;
                        awardCount = 0;
                        String op = adapter.submission.getAuthor();
                        for (CommentObject o : adapter.currentComments) {
                            if (o.comment != null && !(o instanceof MoreChildItem)) {
                                if (o.comment.isTopLevel()) parentCount++;
                                if (o.comment.getComment().getTimesGilded() > 0
                                        || o.comment.getComment().getTimesSilvered() > 0
                                        || o.comment.getComment().getTimesPlatinized() > 0) awardCount++;
                                if (o.comment.getComment().getAuthor() != null
                                        && o.comment.getComment().getAuthor().equals(op)) {
                                    opCount++;
                                }
                                if (o.comment.getComment().getDataNode().has("body_html")
                                        && o.comment.getComment()
                                        .getDataNode()
                                        .get("body_html")
                                        .asText()
                                        .contains("&lt;/a")) {
                                    linkCount++;
                                }
                            }
                        }
                        new AlertDialogWrapper.Builder(getActivity()).setTitle(
                                R.string.set_nav_mode).setSingleChoiceItems(Reddit.stringToArray(

                                "Parent comment ("
                                        + parentCount
                                        + ")"
                                        + ","
                                        +
                                        "Children comment (highlight child comment & navigate)"
                                        + ","
                                        +
                                        "OP ("
                                        + opCount
                                        + ")"
                                        + ","
                                        + "Time"
                                        + ","
                                        + "Link ("
                                        + linkCount
                                        + ")"
                                        + ","
                                        +
                                        ((Authentication.isLoggedIn) ? "You" + "," : "")
                                        +
                                        "Awarded ("
                                        + awardCount
                                        + ")")
                                        .toArray(new String[Authentication.isLoggedIn ? 6 : 5]),
                                getCurrentSort(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                currentSort = CommentNavType.PARENTS;
                                                break;
                                            case 1:
                                                currentSort = CommentNavType.CHILDREN;
                                                break;
                                            case 2:
                                                currentSort = CommentNavType.OP;
                                                break;
                                            case 3:
                                                currentSort = CommentNavType.TIME;
                                                LayoutInflater inflater =
                                                        getActivity().getLayoutInflater();
                                                final View dialoglayout =
                                                        inflater.inflate(R.layout.commenttime,
                                                                null);
                                                final AlertDialogWrapper.Builder builder =
                                                        new AlertDialogWrapper.Builder(
                                                                getActivity());
                                                final Slider landscape =
                                                        dialoglayout.findViewById(R.id.landscape);

                                                final TextView since =
                                                        dialoglayout.findViewById(R.id.time_string);
                                                landscape.setValueRange(60, 18000, false);
                                                landscape.setOnPositionChangeListener(
                                                        new Slider.OnPositionChangeListener() {
                                                            @Override
                                                            public void onPositionChanged(
                                                                    Slider slider, boolean b,
                                                                    float v, float v1, int i,
                                                                    int i1) {
                                                                Calendar c = Calendar.getInstance();
                                                                sortTime = c.getTimeInMillis()
                                                                        - i1 * 1000;

                                                                int commentcount = 0;
                                                                for (CommentObject o : adapter.currentComments) {
                                                                    if (o.comment != null
                                                                            && o.comment.getComment()
                                                                            .getDataNode()
                                                                            .has("created")
                                                                            && o.comment.getComment()
                                                                            .getCreated()
                                                                            .getTime() > sortTime) {
                                                                        commentcount += 1;
                                                                    }
                                                                }
                                                                since.setText(TimeUtils.getTimeAgo(
                                                                        sortTime, getActivity())
                                                                        + " ("
                                                                        + commentcount
                                                                        + " comments)");
                                                            }
                                                        });
                                                landscape.setValue(600, false);
                                                builder.setView(dialoglayout);
                                                builder.setPositiveButton(R.string.btn_set, null)
                                                        .show();
                                                break;
                                            case 5:
                                                currentSort = (Authentication.isLoggedIn ? CommentNavType.YOU
                                                        : CommentNavType.GILDED); // gilded is 5 if not logged in
                                                break;
                                            case 4:
                                                currentSort = CommentNavType.LINK;
                                                break;
                                            case 6:
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

        if (SettingValues.voteGestures) {
            v.findViewById(R.id.up).setOnTouchListener(new OnFlingGestureListener() {
                @Override
                public void onRightToLeft() {
                }

                @Override
                public void onLeftToRight() {
                }

                @Override
                public void onBottomToTop() {
                    adapter.submissionViewHolder.upvote.performClick();
                    Context context = getContext();
                    int duration = Toast.LENGTH_SHORT;
                    CharSequence text;
                    if (!upvoted) {
                        text = getString(R.string.profile_upvoted);
                        downvoted = false;
                    } else {
                        text = getString(R.string.vote_removed);
                    }
                    upvoted = !upvoted;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

                @Override
                public void onTopToBottom() {
                }
            });
        }

        if (SettingValues.voteGestures) {
            v.findViewById(R.id.down).setOnTouchListener(new OnFlingGestureListener() {
                @Override
                public void onRightToLeft() {
                }

                @Override
                public void onLeftToRight() {
                }

                @Override
                public void onBottomToTop() {
                    adapter.submissionViewHolder.downvote.performClick();
                    Context context = getContext();
                    int duration = Toast.LENGTH_SHORT;
                    CharSequence text;
                    if (!downvoted) {
                        text = getString(R.string.profile_downvoted);
                        upvoted = false;
                    } else {
                        text = getString(R.string.vote_removed);
                    }
                    downvoted = !downvoted;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

                @Override
                public void onTopToBottom() {
                }
            });
        }


        toolbar.setBackgroundColor(Palette.getColor(subreddit));

        mSwipeRefreshLayout = v.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(subreddit, getActivity()));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (comments != null) {
                    comments.loadMore(adapter, subreddit, true);
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                //TODO catch errors
            }
        });

        toolbar.setTitle(subreddit);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.menu_comment_items);
        toolbar.setOnMenuItemClickListener(this);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LinearLayoutManager) rv.getLayoutManager()).scrollToPositionWithOffset(1,
                        headerHeight);
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
                    })
                    .setPositiveButton(R.string.btn_offline, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Reddit.appRestart.edit().putBoolean("forceoffline", true).commit();
                            Reddit.forceRestart(getActivity(), false);
                        }
                    })
                    .show();
        }

        if (!(getActivity() instanceof CommentsScreen)
                || ((CommentsScreen) getActivity()).currentPage == page) {
            doAdapter(true);
        } else {
            doAdapter(false);
        }
        return v;
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search: {
                if (comments.comments != null && comments.submission != null) {
                    DataShare.sharedComments = comments.comments;
                    DataShare.subAuthor = comments.submission.getAuthor();
                    Intent i = new Intent(getActivity(), CommentSearch.class);
                    if (getActivity() instanceof MainActivity) {
                        getActivity().startActivityForResult(i, 423);
                    } else {
                        startActivityForResult(i, 423);
                    }
                }
            }
            return true;
            case R.id.sidebar:
                doSidebarOpen();
                return true;
            case R.id.related:
                if (adapter.submission.isSelfPost()) {
                    new AlertDialogWrapper.Builder(getActivity()).setTitle(
                            "Selftext posts have no related submissions")
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                } else {
                    Intent i = new Intent(getActivity(), Related.class);
                    i.putExtra(Related.EXTRA_URL, adapter.submission.getUrl());
                    startActivity(i);
                }
                return true;
            case R.id.shadowbox:
                if (SettingValues.isPro) {
                    if (comments.comments != null && comments.submission != null) {
                        ShadowboxComments.comments = new ArrayList<>();
                        for (CommentObject c : comments.comments) {
                            if (c instanceof CommentItem) {
                                if (c.comment.getComment()
                                        .getDataNode()
                                        .get("body_html")
                                        .asText()
                                        .contains("&lt;/a")) {
                                    String body = c.comment.getComment()
                                            .getDataNode()
                                            .get("body_html")
                                            .asText();
                                    String url;
                                    String[] split = body.split("&lt;a href=\"");
                                    if (split.length > 1) {
                                        for (String chunk : split) {
                                            url = chunk.substring(0,
                                                    chunk.indexOf("\"", 1));
                                            ContentType.Type t =
                                                    ContentType.getContentType(url);

                                            if (ContentType.mediaType(t)) {
                                                ShadowboxComments.comments.add(
                                                        new CommentUrlObject(c.comment,
                                                                url, subreddit));
                                            }

                                        }
                                    } else {
                                        int start = body.indexOf("&lt;a href=\"");
                                        url = body.substring(start,
                                                body.indexOf("\"", start + 1));
                                        ContentType.Type t =
                                                ContentType.getContentType(url);

                                        if (ContentType.mediaType(t)) {
                                            ShadowboxComments.comments.add(
                                                    new CommentUrlObject(c.comment, url, subreddit));
                                        }

                                    }
                                }
                            }
                        }
                        if (!ShadowboxComments.comments.isEmpty()) {
                            Intent i = new Intent(getActivity(), ShadowboxComments.class);
                            startActivity(i);
                        } else {
                            Snackbar.make(mSwipeRefreshLayout,
                                    R.string.shadowbox_comments_nolinks,
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    AlertDialogWrapper.Builder b =
                            new AlertDialogWrapper.Builder(getContext()).setTitle(
                                    R.string.general_shadowbox_comments_ispro)
                                    .setMessage(R.string.pro_upgrade_msg)
                                    .setPositiveButton(R.string.btn_yes_exclaim,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {
                                                    try {
                                                        startActivity(new Intent(
                                                                Intent.ACTION_VIEW,
                                                                Uri.parse(
                                                                        "market://details?id="
                                                                                + getString(
                                                                                R.string.ui_unlock_package))));
                                                    } catch (ActivityNotFoundException e) {
                                                        startActivity(new Intent(
                                                                Intent.ACTION_VIEW,
                                                                Uri.parse(
                                                                        "http://play.google.com/store/apps/details?id="
                                                                                + getString(
                                                                                R.string.ui_unlock_package))));
                                                    }
                                                }
                                            })
                                    .setNegativeButton(R.string.btn_no_danks,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {
                                                    dialog.dismiss();
                                                }
                                            });
                    b.show();
                }
                return true;
            case R.id.sort: {
                openPopup(toolbar);
                return true;
            }
            case R.id.content: {
                if (adapter != null && adapter.submission != null) {
                    if (!PostMatch.openExternal(adapter.submission.getUrl())) {
                        ContentType.Type type =
                                ContentType.getContentType(adapter.submission);
                        switch (type) {
                            case VID_ME:
                            case STREAMABLE:
                                if (SettingValues.video) {
                                    Intent myIntent =
                                            new Intent(getActivity(), MediaView.class);
                                    myIntent.putExtra(MediaView.SUBREDDIT, subreddit);
                                    myIntent.putExtra(MediaView.EXTRA_URL,
                                            adapter.submission.getUrl());
                                    getActivity().startActivity(myIntent);

                                } else {
                                    LinkUtil.openExternally(adapter.submission.getUrl());
                                }
                                break;
                            case IMGUR:
                            case XKCD:
                                Intent i2 = new Intent(getActivity(), MediaView.class);
                                i2.putExtra(MediaView.SUBREDDIT, subreddit);
                                if (adapter.submission.getDataNode().has("preview")
                                        && adapter.submission.getDataNode()
                                        .get("preview")
                                        .get("images")
                                        .get(0)
                                        .get("source")
                                        .has("height")
                                        && type
                                        != ContentType.Type.XKCD) { //Load the preview image which has probably already been cached in memory instead of the direct link
                                    String previewUrl = adapter.submission.getDataNode()
                                            .get("preview")
                                            .get("images")
                                            .get(0)
                                            .get("source")
                                            .get("url")
                                            .asText();
                                    i2.putExtra(MediaView.EXTRA_DISPLAY_URL, previewUrl);
                                }
                                i2.putExtra(MediaView.EXTRA_URL,
                                        adapter.submission.getUrl());
                                getActivity().startActivity(i2);
                                break;
                            case EMBEDDED:
                                if (SettingValues.video) {
                                    String data = adapter.submission.getDataNode()
                                            .get("media_embed")
                                            .get("content")
                                            .asText();
                                    {
                                        Intent i = new Intent(getActivity(),
                                                FullscreenVideo.class);
                                        i.putExtra(FullscreenVideo.EXTRA_HTML, data);
                                        getActivity().startActivity(i);
                                    }
                                } else {
                                    LinkUtil.openExternally(adapter.submission.getUrl());
                                }
                                break;
                            case REDDIT:
                                PopulateSubmissionViewHolder.openRedditContent(
                                        adapter.submission.getUrl(), getActivity());
                                break;
                            case LINK:
                                LinkUtil.openUrl(adapter.submission.getUrl(),
                                        Palette.getColor(
                                                adapter.submission.getSubredditName()),
                                        getActivity());
                                break;
                            case NONE:
                            case SELF:
                                if (adapter.submission.getSelftext().isEmpty()) {
                                    Snackbar s =
                                            Snackbar.make(rv, R.string.submission_nocontent,
                                                    Snackbar.LENGTH_SHORT);
                                    View view = s.getView();
                                    TextView tv = view.findViewById(
                                            android.support.design.R.id.snackbar_text);
                                    tv.setTextColor(Color.WHITE);
                                    s.show();

                                } else {
                                    LayoutInflater inflater =
                                            getActivity().getLayoutInflater();
                                    final View dialoglayout =
                                            inflater.inflate(R.layout.parent_comment_dialog,
                                                    null);
                                    final AlertDialogWrapper.Builder builder =
                                            new AlertDialogWrapper.Builder(getActivity());
                                    adapter.setViews(adapter.submission.getDataNode()
                                                    .get("selftext_html")
                                                    .asText(),
                                            adapter.submission.getSubredditName(),
                                            (SpoilerRobotoTextView) dialoglayout.findViewById(
                                                    R.id.firstTextView),
                                            (CommentOverflow) dialoglayout.findViewById(
                                                    R.id.commentOverflow));
                                    builder.setView(dialoglayout);
                                    builder.show();
                                }
                                break;
                            case ALBUM:
                                if (SettingValues.album) {
                                    if (SettingValues.albumSwipe) {
                                        Intent i =
                                                new Intent(getActivity(), AlbumPager.class);
                                        i.putExtra(Album.EXTRA_URL,
                                                adapter.submission.getUrl());
                                        i.putExtra(AlbumPager.SUBREDDIT, subreddit);
                                        getActivity().startActivity(i);
                                        getActivity().overridePendingTransition(
                                                R.anim.slideright, R.anim.fade_out);
                                    } else {
                                        Intent i = new Intent(getActivity(), Album.class);
                                        i.putExtra(Album.EXTRA_URL,
                                                adapter.submission.getUrl());
                                        i.putExtra(Album.SUBREDDIT, subreddit);
                                        getActivity().startActivity(i);
                                        getActivity().overridePendingTransition(
                                                R.anim.slideright, R.anim.fade_out);
                                    }
                                } else {
                                    LinkUtil.openExternally(adapter.submission.getUrl());

                                }
                                break;
                            case TUMBLR:
                                if (SettingValues.image) {
                                    if (SettingValues.albumSwipe) {
                                        Intent i = new Intent(getActivity(),
                                                TumblrPager.class);
                                        i.putExtra(Album.EXTRA_URL,
                                                adapter.submission.getUrl());
                                        i.putExtra(TumblrPager.SUBREDDIT, subreddit);
                                        getActivity().startActivity(i);
                                        getActivity().overridePendingTransition(
                                                R.anim.slideright, R.anim.fade_out);
                                    } else {
                                        Intent i = new Intent(getActivity(), Tumblr.class);
                                        i.putExtra(Tumblr.SUBREDDIT, subreddit);
                                        i.putExtra(Album.EXTRA_URL,
                                                adapter.submission.getUrl());
                                        getActivity().startActivity(i);
                                        getActivity().overridePendingTransition(
                                                R.anim.slideright, R.anim.fade_out);
                                    }
                                } else {
                                    LinkUtil.openExternally(adapter.submission.getUrl());

                                }
                                break;
                            case IMAGE:
                                PopulateSubmissionViewHolder.openImage(type, getActivity(),
                                        adapter.submission, null, -1);
                                break;
                            case VREDDIT_REDIRECT:
                            case VREDDIT_DIRECT:
                            case GIF:
                                PopulateSubmissionViewHolder.openGif(getActivity(),
                                        adapter.submission, -1);
                                break;
                            case VIDEO:
                                if (!LinkUtil.tryOpenWithVideoPlugin(
                                        adapter.submission.getUrl())) {
                                    LinkUtil.openUrl(adapter.submission.getUrl(),
                                            Palette.getStatusBarColor(), getActivity());
                                }
                        }
                    } else {
                        LinkUtil.openExternally(adapter.submission.getUrl());
                    }
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

    private void doSidebarOpen() {
        new AsyncGetSubreddit().execute(subreddit);
    }

    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(final Subreddit baseSub) {
            try {
                d.dismiss();
            } catch (Exception e) {

            }
            if (baseSub != null) {
                currentlySubbed = Authentication.isLoggedIn && baseSub.isUserSubscriber();
                subreddit = baseSub.getDisplayName();
                try {
                    View sidebar =
                            getActivity().getLayoutInflater().inflate(R.layout.subinfo, null);
                    {
                        sidebar.findViewById(R.id.loader).setVisibility(View.GONE);
                        sidebar.findViewById(R.id.sidebar_text).setVisibility(View.GONE);
                        sidebar.findViewById(R.id.sub_title).setVisibility(View.GONE);
                        sidebar.findViewById(R.id.subscribers).setVisibility(View.GONE);
                        sidebar.findViewById(R.id.active_users).setVisibility(View.GONE);

                        sidebar.findViewById(R.id.header_sub)
                                .setBackgroundColor(Palette.getColor(subreddit));
                        ((TextView) sidebar.findViewById(R.id.sub_infotitle)).setText(subreddit);

                        //Sidebar buttons should use subreddit's accent color
                        int subColor = new ColorPreferences(getContext()).getColor(subreddit);
                        ((TextView) sidebar.findViewById(R.id.theme_text)).setTextColor(subColor);
                        ((TextView) sidebar.findViewById(R.id.wiki_text)).setTextColor(subColor);
                        ((TextView) sidebar.findViewById(R.id.post_text)).setTextColor(subColor);
                        ((TextView) sidebar.findViewById(R.id.mods_text)).setTextColor(subColor);
                        ((TextView) sidebar.findViewById(R.id.flair_text)).setTextColor(subColor);
                    }
                    {
                        sidebar.findViewById(R.id.loader).setVisibility(View.VISIBLE);
                        loaded = true;

                        final View dialoglayout = sidebar;
                        {
                            View submit = (dialoglayout.findViewById(R.id.submit));

                            if (!Authentication.isLoggedIn || !Authentication.didOnline) {
                                submit.setVisibility(View.GONE);
                            }
                            if (SettingValues.fab && SettingValues.fabType == Constants.FAB_POST) {
                                submit.setVisibility(View.GONE);
                            }

                            submit.setOnClickListener(new OnSingleClickListener() {
                                @Override
                                public void onSingleClick(View view) {
                                    Intent inte = new Intent(getActivity(), Submit.class);
                                    inte.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);

                                    getActivity().startActivity(inte);
                                }
                            });
                        }

                        dialoglayout.findViewById(R.id.wiki)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(getActivity(), Wiki.class);
                                        i.putExtra(Wiki.EXTRA_SUBREDDIT, subreddit);
                                        startActivity(i);
                                    }
                                });
                        dialoglayout.findViewById(R.id.submit)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(getActivity(), Submit.class);
                                        i.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                                        startActivity(i);
                                    }
                                });
                        dialoglayout.findViewById(R.id.syncflair)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                       ImageFlairs.syncFlairs(getContext(), subreddit);
                                    }
                                });
                        dialoglayout.findViewById(R.id.theme)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int style = new ColorPreferences(
                                                getActivity()).getThemeSubreddit(subreddit);

                                        final Context contextThemeWrapper =
                                                new ContextThemeWrapper(getActivity(), style);
                                        LayoutInflater localInflater =
                                                getActivity().getLayoutInflater()
                                                        .cloneInContext(contextThemeWrapper);

                                        final View dialoglayout =
                                                localInflater.inflate(R.layout.colorsub, null);

                                        ArrayList<String> arrayList = new ArrayList<>();
                                        arrayList.add(subreddit);
                                        SettingsSubAdapter.showSubThemeEditor(arrayList,
                                                getActivity(), dialoglayout);
                                    }
                                });
                        dialoglayout.findViewById(R.id.mods)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final Dialog d =
                                                new MaterialDialog.Builder(getActivity()).title(
                                                        R.string.sidebar_findingmods)
                                                        .cancelable(true)
                                                        .content(R.string.misc_please_wait)
                                                        .progress(true, 100)
                                                        .show();
                                        new AsyncTask<Void, Void, Void>() {
                                            ArrayList<UserRecord> mods;

                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                mods = new ArrayList<>();
                                                UserRecordPaginator paginator =
                                                        new UserRecordPaginator(
                                                                Authentication.reddit, subreddit,
                                                                "moderators");
                                                paginator.setSorting(Sorting.HOT);
                                                paginator.setTimePeriod(TimePeriod.ALL);
                                                while (paginator.hasNext()) {
                                                    mods.addAll(paginator.next());
                                                }
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(Void aVoid) {
                                                final ArrayList<String> names = new ArrayList<>();
                                                for (UserRecord rec : mods) {
                                                    names.add(rec.getFullName());
                                                }
                                                d.dismiss();
                                                new MaterialDialog.Builder(getActivity()).title(
                                                        getString(R.string.sidebar_submods,
                                                                subreddit))
                                                        .items(names)
                                                        .itemsCallback(
                                                                new MaterialDialog.ListCallback() {
                                                                    @Override
                                                                    public void onSelection(
                                                                            MaterialDialog dialog,
                                                                            View itemView,
                                                                            int which,
                                                                            CharSequence text) {
                                                                        Intent i = new Intent(
                                                                                getActivity(),
                                                                                Profile.class);
                                                                        i.putExtra(
                                                                                Profile.EXTRA_PROFILE,
                                                                                names.get(which));
                                                                        startActivity(i);
                                                                    }
                                                                })
                                                        .positiveText(R.string.btn_message)
                                                        .onPositive(
                                                                new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(
                                                                            @NonNull MaterialDialog dialog,
                                                                            @NonNull DialogAction which) {
                                                                        Intent i = new Intent(
                                                                                getActivity(),
                                                                                SendMessage.class);
                                                                        i.putExtra(
                                                                                SendMessage.EXTRA_NAME,
                                                                                "/r/" + subreddit);
                                                                        startActivity(i);
                                                                    }
                                                                })
                                                        .show();
                                            }
                                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    }
                                });
                        dialoglayout.findViewById(R.id.flair).setVisibility(View.GONE);

                    }
                    {
                        sidebar.findViewById(R.id.loader).setVisibility(View.GONE);

                        if (baseSub.getSidebar() != null && !baseSub.getSidebar().isEmpty()) {
                            sidebar.findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

                            final String text =
                                    baseSub.getDataNode().get("description_html").asText();
                            final SpoilerRobotoTextView body =
                                    sidebar.findViewById(R.id.sidebar_text);
                            CommentOverflow overflow = sidebar.findViewById(R.id.commentOverflow);
                            setViews(text, baseSub.getDisplayName(), body, overflow);
                        } else {
                            sidebar.findViewById(R.id.sidebar_text).setVisibility(View.GONE);
                        }
                        View collection = sidebar.findViewById(R.id.collection);
                        if (Authentication.isLoggedIn) {
                            collection.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AsyncTask<Void, Void, Void>() {
                                        HashMap<String, MultiReddit> multis =
                                                new HashMap<String, MultiReddit>();

                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            if (UserSubscriptions.multireddits == null) {
                                                UserSubscriptions.syncMultiReddits(getContext());
                                            }
                                            for (MultiReddit r : UserSubscriptions.multireddits) {
                                                multis.put(r.getDisplayName(), r);
                                            }
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void aVoid) {
                                            new MaterialDialog.Builder(getContext()).title(
                                                    "Add /r/" + baseSub.getDisplayName() + " to")
                                                    .items(multis.keySet())
                                                    .itemsCallback(
                                                            new MaterialDialog.ListCallback() {
                                                                @Override
                                                                public void onSelection(
                                                                        MaterialDialog dialog,
                                                                        View itemView,
                                                                        final int which,
                                                                        CharSequence text) {
                                                                    new AsyncTask<Void, Void, Void>() {
                                                                        @Override
                                                                        protected Void doInBackground(
                                                                                Void... params) {
                                                                            try {
                                                                                final String
                                                                                        multiName =
                                                                                        multis.keySet()
                                                                                                .toArray(
                                                                                                        new String[multis
                                                                                                                .size()])[which];
                                                                                List<String> subs =
                                                                                        new ArrayList<String>();
                                                                                for (MultiSubreddit sub : multis
                                                                                        .get(multiName)
                                                                                        .getSubreddits()) {
                                                                                    subs.add(
                                                                                            sub.getDisplayName());
                                                                                }
                                                                                subs.add(
                                                                                        baseSub.getDisplayName());
                                                                                new MultiRedditManager(
                                                                                        Authentication.reddit)
                                                                                        .createOrUpdate(
                                                                                                new MultiRedditUpdateRequest.Builder(
                                                                                                        Authentication.name,
                                                                                                        multiName)
                                                                                                        .subreddits(
                                                                                                                subs)
                                                                                                        .build());

                                                                                UserSubscriptions.syncMultiReddits(
                                                                                        getContext());

                                                                                getActivity().runOnUiThread(
                                                                                        new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                Snackbar.make(
                                                                                                        toolbar,
                                                                                                        getString(
                                                                                                                R.string.multi_subreddit_added,
                                                                                                                multiName),
                                                                                                        Snackbar.LENGTH_LONG)
                                                                                                        .show();
                                                                                            }
                                                                                        });
                                                                            } catch (final NetworkException | ApiException e) {
                                                                                getActivity().runOnUiThread(
                                                                                        new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                getActivity()
                                                                                                        .runOnUiThread(
                                                                                                                new Runnable() {
                                                                                                                    @Override
                                                                                                                    public void run() {
                                                                                                                        Snackbar.make(
                                                                                                                                toolbar,
                                                                                                                                getString(
                                                                                                                                        R.string.multi_error),
                                                                                                                                Snackbar.LENGTH_LONG)
                                                                                                                                .setAction(
                                                                                                                                        R.string.btn_ok,
                                                                                                                                        new View.OnClickListener() {
                                                                                                                                            @Override
                                                                                                                                            public void onClick(
                                                                                                                                                    View v) {

                                                                                                                                            }
                                                                                                                                        })
                                                                                                                                .show();
                                                                                                                    }
                                                                                                                });
                                                                                            }
                                                                                        });
                                                                                e.printStackTrace();
                                                                            }
                                                                            return null;
                                                                        }
                                                                    }.executeOnExecutor(
                                                                            AsyncTask.THREAD_POOL_EXECUTOR);

                                                                }
                                                            })
                                                    .show();
                                        }
                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            });

                        } else {
                            collection.setVisibility(View.GONE);
                        }

                        {
                            final TextView subscribe = sidebar.findViewById(R.id.subscribe);

                            currentlySubbed = (!Authentication.isLoggedIn
                                    && UserSubscriptions.getSubscriptions(getActivity())
                                    .contains(baseSub.getDisplayName().toLowerCase(Locale.ENGLISH))) || (
                                    Authentication.isLoggedIn
                                            && baseSub.isUserSubscriber());
                            doSubscribeButtonText(currentlySubbed, subscribe);

                            subscribe.setOnClickListener(new View.OnClickListener() {
                                private void doSubscribe() {
                                    if (Authentication.isLoggedIn) {
                                        new AlertDialogWrapper.Builder(getActivity()).setTitle(
                                                getString(R.string.subscribe_to,
                                                        baseSub.getDisplayName()))
                                                .setPositiveButton(R.string.reorder_add_subscribe,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                new AsyncTask<Void, Void, Boolean>() {
                                                                    @Override
                                                                    public void onPostExecute(
                                                                            Boolean success) {
                                                                        if (!success) { // If subreddit was removed from account or not

                                                                            new AlertDialogWrapper.Builder(
                                                                                    getActivity()).setTitle(
                                                                                    R.string.force_change_subscription)
                                                                                    .setMessage(
                                                                                            R.string.force_change_subscription_desc)
                                                                                    .setPositiveButton(
                                                                                            R.string.btn_yes,
                                                                                            new DialogInterface.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(
                                                                                                        DialogInterface dialog,
                                                                                                        int which) {
                                                                                                    changeSubscription(
                                                                                                            baseSub,
                                                                                                            true); // Force add the subscription
                                                                                                    Snackbar
                                                                                                            s =
                                                                                                            Snackbar.make(
                                                                                                                    toolbar,
                                                                                                                    getString(
                                                                                                                            R.string.misc_subscribed),
                                                                                                                    Snackbar.LENGTH_SHORT);
                                                                                                    View
                                                                                                            view =
                                                                                                            s.getView();
                                                                                                    TextView
                                                                                                            tv =
                                                                                                            view
                                                                                                                    .findViewById(
                                                                                                                            android.support.design.R.id.snackbar_text);
                                                                                                    tv.setTextColor(
                                                                                                            Color.WHITE);
                                                                                                    s.show();
                                                                                                }
                                                                                            })
                                                                                    .setNegativeButton(
                                                                                            R.string.btn_no,
                                                                                            new DialogInterface.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(
                                                                                                        DialogInterface dialog,
                                                                                                        int which) {

                                                                                                }
                                                                                            })
                                                                                    .setCancelable(
                                                                                            false)
                                                                                    .show();
                                                                        } else {
                                                                            changeSubscription(
                                                                                    baseSub, true);
                                                                        }

                                                                    }

                                                                    @Override
                                                                    protected Boolean doInBackground(
                                                                            Void... params) {
                                                                        try {
                                                                            new AccountManager(
                                                                                    Authentication.reddit)
                                                                                    .subscribe(
                                                                                            baseSub);
                                                                        } catch (NetworkException e) {
                                                                            return false; // Either network crashed or trying to unsubscribe to a subreddit that the account isn't subscribed to
                                                                        }
                                                                        return true;
                                                                    }
                                                                }.executeOnExecutor(
                                                                        AsyncTask.THREAD_POOL_EXECUTOR);
                                                            }
                                                        })
                                                .setNegativeButton(R.string.btn_cancel, null)
                                                .setNeutralButton(R.string.btn_add_to_sublist,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                changeSubscription(baseSub,
                                                                        true); // Force add the subscription
                                                                Snackbar s = Snackbar.make(toolbar,
                                                                        R.string.sub_added,
                                                                        Snackbar.LENGTH_SHORT);
                                                                View view = s.getView();
                                                                TextView tv = view.findViewById(
                                                                        android.support.design.R.id.snackbar_text);
                                                                tv.setTextColor(Color.WHITE);
                                                                s.show();
                                                            }
                                                        })
                                                .show();
                                    } else {
                                        changeSubscription(baseSub, true);
                                    }
                                }

                                @Override
                                public void onClick(View v) {
                                    if (!currentlySubbed) {
                                        doSubscribe();
                                        doSubscribeButtonText(currentlySubbed, subscribe);
                                    } else {
                                        doUnsubscribe();
                                        doSubscribeButtonText(currentlySubbed, subscribe);
                                    }
                                }

                                private void doUnsubscribe() {
                                    if (Authentication.didOnline) {
                                        new AlertDialogWrapper.Builder(getContext()).setTitle(
                                                getString(R.string.unsubscribe_from,
                                                        baseSub.getDisplayName()))
                                                .setPositiveButton(
                                                        R.string.reorder_remove_unsubsribe,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                new AsyncTask<Void, Void, Boolean>() {
                                                                    @Override
                                                                    public void onPostExecute(
                                                                            Boolean success) {
                                                                        if (!success) { // If subreddit was removed from account or not

                                                                            new AlertDialogWrapper.Builder(
                                                                                    getContext()).setTitle(
                                                                                    R.string.force_change_subscription)
                                                                                    .setMessage(
                                                                                            R.string.force_change_subscription_desc)
                                                                                    .setPositiveButton(
                                                                                            R.string.btn_yes,
                                                                                            new DialogInterface.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(
                                                                                                        DialogInterface dialog,
                                                                                                        int which) {
                                                                                                    changeSubscription(
                                                                                                            baseSub,
                                                                                                            false); // Force add the subscription
                                                                                                    Snackbar
                                                                                                            s =
                                                                                                            Snackbar.make(
                                                                                                                    toolbar,
                                                                                                                    getString(
                                                                                                                            R.string.misc_unsubscribed),
                                                                                                                    Snackbar.LENGTH_SHORT);
                                                                                                    View
                                                                                                            view =
                                                                                                            s.getView();
                                                                                                    TextView
                                                                                                            tv =
                                                                                                            view
                                                                                                                    .findViewById(
                                                                                                                            android.support.design.R.id.snackbar_text);
                                                                                                    tv.setTextColor(
                                                                                                            Color.WHITE);
                                                                                                    s.show();
                                                                                                }
                                                                                            })
                                                                                    .setNegativeButton(
                                                                                            R.string.btn_no,
                                                                                            new DialogInterface.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(
                                                                                                        DialogInterface dialog,
                                                                                                        int which) {

                                                                                                }
                                                                                            })
                                                                                    .setCancelable(
                                                                                            false)
                                                                                    .show();
                                                                        } else {
                                                                            changeSubscription(
                                                                                    baseSub, false);
                                                                        }

                                                                    }

                                                                    @Override
                                                                    protected Boolean doInBackground(
                                                                            Void... params) {
                                                                        try {
                                                                            new AccountManager(
                                                                                    Authentication.reddit)
                                                                                    .unsubscribe(
                                                                                            baseSub);
                                                                        } catch (NetworkException e) {
                                                                            return false; // Either network crashed or trying to unsubscribe to a subreddit that the account isn't subscribed to
                                                                        }
                                                                        return true;
                                                                    }
                                                                }.executeOnExecutor(
                                                                        AsyncTask.THREAD_POOL_EXECUTOR);
                                                            }
                                                        })
                                                .setNeutralButton(R.string.just_unsub,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                changeSubscription(baseSub,
                                                                        false); // Force add the subscription
                                                                Snackbar s = Snackbar.make(toolbar,
                                                                        R.string.misc_unsubscribed,
                                                                        Snackbar.LENGTH_SHORT);
                                                                View view = s.getView();
                                                                TextView tv = view.findViewById(
                                                                        android.support.design.R.id.snackbar_text);
                                                                tv.setTextColor(Color.WHITE);
                                                                s.show();
                                                            }
                                                        })
                                                .setNegativeButton(R.string.btn_cancel, null)
                                                .show();
                                    } else {
                                        changeSubscription(baseSub, false);
                                    }
                                }


                            });

                        }
                        if (!baseSub.getPublicDescription().isEmpty()) {
                            sidebar.findViewById(R.id.sub_title).setVisibility(View.VISIBLE);
                            setViews(baseSub.getDataNode().get("public_description_html").asText(),
                                    baseSub.getDisplayName().toLowerCase(Locale.ENGLISH),
                                    ((SpoilerRobotoTextView) sidebar.findViewById(R.id.sub_title)),
                                    (CommentOverflow) sidebar.findViewById(
                                            R.id.sub_title_overflow));
                        } else {
                            sidebar.findViewById(R.id.sub_title).setVisibility(View.GONE);
                        }
                        if (baseSub.getDataNode().has("icon_img") && !baseSub.getDataNode()
                                .get("icon_img")
                                .asText()
                                .isEmpty()) {
                            ((Reddit) getContext().getApplicationContext()).getImageLoader()
                                    .displayImage(baseSub.getDataNode().get("icon_img").asText(),
                                            (ImageView) sidebar.findViewById(R.id.subimage));
                        } else {
                            sidebar.findViewById(R.id.subimage).setVisibility(View.GONE);
                        }
                        String bannerImage = baseSub.getBannerImage();
                        if (bannerImage != null && !bannerImage.isEmpty()) {
                            sidebar.findViewById(R.id.sub_banner).setVisibility(View.VISIBLE);
                            ((Reddit) getContext().getApplicationContext()).getImageLoader()
                                    .displayImage(bannerImage,
                                            (ImageView) sidebar.findViewById(R.id.sub_banner));
                        } else {
                            sidebar.findViewById(R.id.sub_banner).setVisibility(View.GONE);
                        }
                        ((TextView) sidebar.findViewById(R.id.subscribers)).setText(
                                getString(R.string.subreddit_subscribers_string,
                                        baseSub.getLocalizedSubscriberCount()));
                        sidebar.findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

                        ((TextView) sidebar.findViewById(R.id.active_users)).setText(
                                getString(R.string.subreddit_active_users_string_new,
                                        baseSub.getLocalizedAccountsActive()));
                        sidebar.findViewById(R.id.active_users).setVisibility(View.VISIBLE);
                    }

                    new AlertDialogWrapper.Builder(getContext()).setPositiveButton(
                            R.string.btn_close, null).setView(sidebar).show();
                } catch (NullPointerException e) { //activity has been killed
                }
            }
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            try {
                return Authentication.reddit.getSubreddit(params[0]);
            } catch (Exception e) {
                try {
                    d.dismiss();
                } catch (Exception ignored) {
                }
                return null;
            }
        }

        Dialog d;

        @Override
        protected void onPreExecute() {
            d = new MaterialDialog.Builder(getActivity()).title(R.string.subreddit_sidebar_progress)
                    .progress(true, 100)
                    .content(R.string.misc_please_wait)
                    .cancelable(false)
                    .show();
        }
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
        if (load) doRefresh(true);
        if (load) loaded = true;
        if (!single
                && getActivity() instanceof CommentsScreen
                && ((CommentsScreen) getActivity()).subredditPosts != null
                && Authentication.didOnline && ((CommentsScreen) getActivity()).currentPosts != null && ((CommentsScreen) getActivity()).currentPosts.size() > page) {
            try {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
            } catch(IndexOutOfBoundsException e){
                return;
            }
            Submission s = ((CommentsScreen) getActivity()).currentPosts.get(page);
            if (s != null && s.getDataNode().has("suggested_sort") && !s.getDataNode()
                    .get("suggested_sort")
                    .asText()
                    .equalsIgnoreCase("null")) {
                String sorting = s.getDataNode().get("suggested_sort").asText().toUpperCase();
                sorting = sorting.replace("İ", "I");
                commentSorting = CommentSort.valueOf(sorting);
            } else if (s != null) {
                commentSorting = SettingValues.getCommentSorting(s.getSubredditName());
            }
            if (load) comments.setSorting(commentSorting);
            if (adapter == null) {
                adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
                rv.setAdapter(adapter);
            }
        } else if (getActivity() instanceof MainActivity) {
            if (Authentication.didOnline) {
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
                Submission s = ((MainActivity) getActivity()).openingComments;
                if (s != null && s.getDataNode().has("suggested_sort") && !s.getDataNode()
                        .get("suggested_sort")
                        .asText()
                        .equalsIgnoreCase("null")) {
                    String sorting = s.getDataNode().get("suggested_sort").asText().toUpperCase();
                    sorting = sorting.replace("İ", "I");
                    commentSorting = CommentSort.valueOf(sorting);
                } else if (s != null) {
                    commentSorting = SettingValues.getCommentSorting(s.getSubredditName());
                }
                if (load) comments.setSorting(commentSorting);
                if (adapter == null) {
                    adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
                    rv.setAdapter(adapter);
                }
            } else {
                Submission s = ((MainActivity) getActivity()).openingComments;
                doRefresh(false);
                comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, s);
                if (adapter == null) {
                    adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
                    rv.setAdapter(adapter);
                }
            }
        } else {
            Submission s = null;
            try {
                s = OfflineSubreddit.getSubmissionFromStorage(
                        fullname.contains("_") ? fullname : "t3_" + fullname, getContext(),
                        !NetworkUtil.isConnected(getActivity()), new ObjectMapper().reader());
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

                    if (s != null) {
                        adapter = new CommentAdapter(this, comments, rv, s, getFragmentManager());
                    }
                    rv.setAdapter(adapter);
                }
            } else {
                if (context.equals(Reddit.EMPTY_STRING)) {
                    comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout);
                    if (load) comments.setSorting(commentSorting);
                } else {
                    comments = new SubmissionComments(fullname, this, mSwipeRefreshLayout, context,
                            contextNumber);
                    if (load) comments.setSorting(commentSorting);
                }
            }
        }
    }

    public void doData(Boolean b) {
        if (adapter == null || single) {
            adapter = new CommentAdapter(this, comments, rv, comments.submission,
                    getFragmentManager());

            rv.setAdapter(adapter);
            adapter.currentSelectedItem = context;

            if (context.isEmpty()) {
                if (SettingValues.collapseCommentsDefault) {
                    adapter.collapseAll();
                }
            }

            adapter.reset(getContext(), comments, rv, comments.submission, b);
        } else if (!b) {
            try {
                adapter.reset(getContext(), comments, rv, (getActivity() instanceof MainActivity)
                        ? ((MainActivity) getActivity()).openingComments : comments.submission, b);
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
        contextNumber = bundle.getInt("contextNumber", 5);
        np = bundle.getBoolean("np", false);
        archived = bundle.getBoolean("archived", false);
        locked = bundle.getBoolean("locked", false);
        contest = bundle.getBoolean("contest", false);

        loadMore = (!context.isEmpty() && !context.equals(Reddit.EMPTY_STRING));
        if (!single) loadMore = false;
        int subredditStyle = new ColorPreferences(getActivity()).getThemeSubreddit(subreddit);
        contextThemeWrapper = new ContextThemeWrapper(getActivity(), subredditStyle);
        mLayoutManager = new PreCachingLayoutManagerComments(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (comments != null) comments.cancelLoad();
        if (adapter != null && adapter.currentComments != null) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText()
                    .toString()
                    .isEmpty()) {
                Drafts.addDraft(adapter.currentlyEditing.getText().toString());
                Toast.makeText(getActivity().getApplicationContext(), R.string.msg_save_draft,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public int getCurrentSort() {
        switch (currentSort) {
            case PARENTS:
                return 0;
            case CHILDREN:
                return 1;
            case TIME:
                return 3;
            case GILDED:
                return 6;
            case OP:
                return 3;
            case YOU:
                return 5;
            case LINK:
                return 4;
        }
        return 0;
    }

    public void resetScroll(boolean override) {
        if (toolbarScroll == null) {
            toolbarScroll = new ToolbarScrollHideHandler(toolbar, v.findViewById(R.id.header),
                    v.findViewById(R.id.progress),
                    SettingValues.commentAutoHide ? v.findViewById(R.id.commentnav) : null) {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (SettingValues.fabComments) {
                        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING
                                && !overrideFab) {
                            diff += dy;
                        } else if (!overrideFab) {
                            diff = 0;
                        }
                        if (fab != null && !overrideFab) {
                            if (dy <= 0 && fab.getId() != 0) {
                                if (recyclerView.getScrollState()
                                        != RecyclerView.SCROLL_STATE_DRAGGING
                                        || diff < -fab.getHeight() * 2) {
                                    fab.show();
                                }
                            } else {
                                fab.hide();
                            }
                        }
                    }
                }
            };
            rv.addOnScrollListener(toolbarScroll);
        } else {
            toolbarScroll.reset = true;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //This is the filter
        if (event.getAction() != KeyEvent.ACTION_DOWN) return true;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            goDown();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            goUp();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return onMenuItemClick(toolbar.getMenu().findItem(R.id.search));
        }
        return false;
    }

    private void reloadSubs() {
        mSwipeRefreshLayout.setRefreshing(true);
        comments.setSorting(commentSorting);
        rv.scrollToPosition(0);
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
                    : commentSorting == CommentSort.TOP ? 1 : commentSorting == CommentSort.NEW ? 2
                            : commentSorting == CommentSort.CONTROVERSIAL ? 3
                                    : commentSorting == CommentSort.OLD ? 4
                                            : commentSorting == CommentSort.QA ? 5 : 0;

            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getActivity());
            builder.setTitle(R.string.sorting_choose);
            Resources res = getActivity().getBaseContext().getResources();
            builder.setSingleChoiceItems(new String[]{
                    res.getString(R.string.sorting_best), res.getString(R.string.sorting_top),
                    res.getString(R.string.sorting_new),
                    res.getString(R.string.sorting_controversial),
                    res.getString(R.string.sorting_old), res.getString(R.string.sorting_ama)
            }, i, l2);
            builder.alwaysCallSingleChoiceCallback();
            builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reloadSubs();
                }
            })
                    .setNeutralButton(getString(R.string.sorting_defaultfor, subreddit),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SettingValues.setDefaultCommentSorting(commentSorting,
                                            subreddit);
                                    reloadSubs();
                                }
                            });
            builder.show();
        }

    }

    public void doGoUp(int old) {
        int depth = -1;
        if (adapter.currentlySelected != null) {
            depth = adapter.currentNode.getDepth();
        }
        int pos = (old < 2) ? 0 : old - 1;

        for (int i = pos - 1; i >= 0; i--) {
            try {
                CommentObject o = adapter.currentComments.get(adapter.getRealPosition(i));
                if (o instanceof CommentItem && pos - 1 != i) {
                    boolean matches = false;
                    switch (currentSort) {

                        case PARENTS:
                            matches = o.comment.isTopLevel();
                            break;
                        case CHILDREN:
                            if (depth == -1) {
                                matches = o.comment.isTopLevel();
                            } else {
                                matches = o.comment.getDepth() == depth;
                                if (matches) {
                                    adapter.currentNode = o.comment;
                                    adapter.currentSelectedItem =
                                            o.comment.getComment().getFullName();
                                }
                            }
                            break;
                        case TIME:
                            matches = (o.comment.getComment() != null
                                    && o.comment.getComment().getCreated().getTime() > sortTime);

                            break;
                        case GILDED:
                            matches = (o.comment.getComment().getTimesGilded() > 0
                                    || o.comment.getComment().getTimesSilvered() > 0
                                    || o.comment.getComment().getTimesPlatinized() > 0);
                            break;
                        case OP:
                            matches = adapter.submission != null && o.comment.getComment()
                                    .getAuthor()
                                    .equals(adapter.submission.getAuthor());
                            break;
                        case YOU:
                            matches = adapter.submission != null && o.comment.getComment()
                                    .getAuthor()
                                    .equals(Authentication.name);
                            break;
                        case LINK:
                            matches = o.comment.getComment()
                                    .getDataNode()
                                    .get("body_html")
                                    .asText()
                                    .contains("&lt;/a");
                            break;
                    }
                    if (matches) {
                        if (i + 2 == old) {
                            doGoUp(old - 1);
                        } else {
                            (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(
                                    i + 2, ((View) toolbar.getParent()).getTranslationY() != 0 ? 0
                                            : (v.findViewById(R.id.header)).getHeight());
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
        if (adapter != null
                && adapter.currentComments != null
                && !adapter.currentComments.isEmpty()) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText()
                    .toString()
                    .isEmpty()) {
                final int finalToGoto = toGoto;
                new AlertDialogWrapper.Builder(getActivity()).setTitle(
                        R.string.discard_comment_title)
                        .setMessage(R.string.comment_discard_msg)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.currentlyEditing = null;
                                doGoUp(finalToGoto);
                            }
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();

            } else {
                doGoUp(toGoto);
            }
        }
    }

    public void doGoDown(int old) {
        int depth = -1;
        if (adapter.currentlySelected != null) {
            depth = adapter.currentNode.getDepth();
        }
        int pos = old - 2;
        if (pos < 0) pos = 0;
        String original = adapter.currentComments.get(adapter.getRealPosition(pos)).getName();
        if (old < 2) {
            (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(
                    2, ((View) toolbar.getParent()).getTranslationY() != 0 ? 0
                            : (v.findViewById(R.id.header).getHeight()));
        } else {
            for (int i = pos + 1; i < adapter.currentComments.size(); i++) {
                try {
                    CommentObject o = adapter.currentComments.get(adapter.getRealPosition(i));
                    if (o instanceof CommentItem) {
                        boolean matches = false;
                        switch (currentSort) {

                            case PARENTS:
                                matches = o.comment.isTopLevel();
                                break;
                            case CHILDREN:
                                if (depth == -1) {
                                    matches = o.comment.isTopLevel();
                                } else {
                                    matches = o.comment.getDepth() == depth;
                                    if (matches) {
                                        adapter.currentNode = o.comment;
                                        adapter.currentSelectedItem =
                                                o.comment.getComment().getFullName();
                                    }
                                }
                                break;
                            case TIME:
                                matches = o.comment.getComment().getCreated().getTime() > sortTime;
                                break;
                            case GILDED:
                                matches = (o.comment.getComment().getTimesGilded() > 0
                                        || o.comment.getComment().getTimesSilvered() > 0
                                        || o.comment.getComment().getTimesPlatinized() > 0);
                                break;
                            case OP:
                                matches = adapter.submission != null && o.comment.getComment()
                                        .getAuthor()
                                        .equals(adapter.submission.getAuthor());
                                break;
                            case YOU:
                                matches = adapter.submission != null && o.comment.getComment()
                                        .getAuthor()
                                        .equals(Authentication.name);
                                break;
                            case LINK:
                                matches = o.comment.getComment()
                                        .getDataNode()
                                        .get("body_html")
                                        .asText()
                                        .contains("&lt;/a");
                                break;
                        }
                        if (matches) {
                            if (o.getName().equals(original)) {
                                doGoDown(i + 2);
                            } else {
                                (((PreCachingLayoutManagerComments) rv.getLayoutManager())).scrollToPositionWithOffset(
                                        i + 2,
                                        ((View) toolbar.getParent()).getTranslationY() != 0 ? 0
                                                : (v.findViewById(R.id.header).getHeight()));
                            }
                            break;
                        }
                    }
                } catch (Exception ignored) {

                }
            }
        }
    }

    private void goDown() {
        ((View) toolbar.getParent()).setTranslationY(-((View) toolbar.getParent()).getHeight());
        int toGoto = mLayoutManager.findFirstVisibleItemPosition();
        if (adapter != null
                && adapter.currentComments != null
                && !adapter.currentComments.isEmpty()) {
            if (adapter.currentlyEditing != null && !adapter.currentlyEditing.getText()
                    .toString()
                    .isEmpty()) {
                final int finalToGoto = toGoto;
                new AlertDialogWrapper.Builder(getActivity()).setTitle(
                        R.string.discard_comment_title)
                        .setMessage(R.string.comment_discard_msg)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.currentlyEditing = null;
                                doGoDown(finalToGoto);
                            }
                        })
                        .setNegativeButton(R.string.btn_no, null)
                        .show();

            } else {
                doGoDown(toGoto);
            }
        }
    }

    private void changeSubscription(Subreddit subreddit, boolean isChecked) {
        UserSubscriptions.addSubreddit(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH), getContext());

        Snackbar s = Snackbar.make(toolbar, isChecked ? getString(R.string.misc_subscribed)
                : getString(R.string.misc_unsubscribed), Snackbar.LENGTH_SHORT);
        View view = s.getView();
        TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        s.show();
    }

    private void setViews(String rawHTML, String subreddit, SpoilerRobotoTextView firstTextView,
            CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subreddit);
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subreddit);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subreddit);
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

    private void doSubscribeButtonText(boolean currentlySubbed, TextView subscribe) {
        if (Authentication.didOnline) {
            if (currentlySubbed) {
                subscribe.setText(R.string.unsubscribe_caps);
            } else {
                subscribe.setText(R.string.subscribe_caps);
            }
        } else {
            if (currentlySubbed) {
                subscribe.setText(R.string.btn_remove_from_sublist);
            } else {
                subscribe.setText(R.string.btn_add_to_sublist);
            }
        }
    }

    CommentNavType currentSort = CommentNavType.PARENTS;
    long           sortTime    = 0;
}