package me.ccrama.redditslide.SubmissionViews;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.PostReadLater;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Tumblr;
import me.ccrama.redditslide.Activities.TumblrPager;
import me.ccrama.redditslide.Adapters.CommentAdapter;
import me.ccrama.redditslide.Adapters.NewsViewHolder;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.CommentCacheAsync;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.ForceTouch.PeekViewActivity;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.ReadLater;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionCache;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;

/**
 * Created by ccrama on 9/19/2015.
 */
public class PopulateNewsViewHolder {

    public PopulateNewsViewHolder() {
    }

    public static int getStyleAttribColorValue(final Context context, final int attribResId,
            final int defaultValue) {
        final TypedValue tv = new TypedValue();
        final boolean found = context.getTheme().resolveAttribute(attribResId, tv, true);
        return found ? tv.data : defaultValue;
    }

    private static void addClickFunctions(final View base, final ContentType.Type type,
            final Activity contextActivity, final Submission submission,
            final NewsViewHolder holder, final boolean full) {
        base.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (NetworkUtil.isConnected(contextActivity) || (!NetworkUtil.isConnected(
                        contextActivity) && ContentType.fullImage(type))) {
                    if (SettingValues.storeHistory && !full) {
                        if (!submission.isNsfw() || SettingValues.storeNSFWHistory) {
                            HasSeen.addSeen(submission.getFullName());
                            if (contextActivity instanceof MainActivity
                                    || contextActivity instanceof MultiredditOverview
                                    || contextActivity instanceof SubredditView
                                    || contextActivity instanceof Search
                                    || contextActivity instanceof Profile) {
                                holder.title.setAlpha(0.54f);
                            }
                        }
                    }
                    if (!(contextActivity instanceof PeekViewActivity)
                            || !((PeekViewActivity) contextActivity).isPeeking()
                            || (base instanceof HeaderImageLinkView
                            && ((HeaderImageLinkView) base).popped)) {
                        if (!PostMatch.openExternal(submission.getUrl())
                                || type == ContentType.Type.VIDEO) {
                            switch (type) {
                                case VID_ME:
                                case STREAMABLE:
                                    if (SettingValues.video) {
                                        Intent myIntent =
                                                new Intent(contextActivity, MediaView.class);
                                        myIntent.putExtra(MediaView.SUBREDDIT,
                                                submission.getSubredditName());
                                        myIntent.putExtra(MediaView.EXTRA_URL, submission.getUrl());
                                        contextActivity.startActivity(myIntent);
                                    } else {
                                        LinkUtil.openExternally(submission.getUrl());
                                    }
                                    break;
                                case IMGUR:
                                    openImage(type, contextActivity, submission, holder.leadImage,
                                            holder.getAdapterPosition());
                                    break;
                                case EMBEDDED:
                                    if (SettingValues.video) {
                                        String data = Html.fromHtml(submission.getDataNode()
                                                .get("media_embed")
                                                .get("content")
                                                .asText()).toString();
                                        {
                                            Intent i = new Intent(contextActivity,
                                                    FullscreenVideo.class);
                                            i.putExtra(FullscreenVideo.EXTRA_HTML, data);
                                            contextActivity.startActivity(i);
                                        }
                                    } else {
                                        LinkUtil.openExternally(submission.getUrl());
                                    }
                                    break;
                                case REDDIT:
                                    openRedditContent(submission.getUrl(), contextActivity);
                                    break;
                                case LINK:
                                    LinkUtil.openUrl(submission.getUrl(),
                                            Palette.getColor(submission.getSubredditName()),
                                            contextActivity, holder.getAdapterPosition(),
                                            submission);
                                    break;
                                case SELF:
                                    if (holder != null) {
                                        OnSingleClickListener.override = true;
                                        holder.itemView.performClick();
                                    }
                                    break;
                                case ALBUM:
                                    if (SettingValues.album) {
                                        Intent i;
                                        if (SettingValues.albumSwipe) {
                                            i = new Intent(contextActivity, AlbumPager.class);
                                            i.putExtra(AlbumPager.SUBREDDIT,
                                                    submission.getSubredditName());
                                            i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        } else {
                                            i = new Intent(contextActivity, Album.class);
                                            i.putExtra(Album.SUBREDDIT,
                                                    submission.getSubredditName());
                                            i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        }
                                        addAdaptorPosition(i, submission,
                                                holder.getAdapterPosition());
                                        contextActivity.startActivity(i);
                                        contextActivity.overridePendingTransition(R.anim.slideright,
                                                R.anim.fade_out);
                                    } else {
                                        LinkUtil.openExternally(submission.getUrl());

                                    }
                                    break;
                                case TUMBLR:
                                    if (SettingValues.album) {
                                        Intent i;
                                        if (SettingValues.albumSwipe) {
                                            i = new Intent(contextActivity, TumblrPager.class);
                                            i.putExtra(TumblrPager.SUBREDDIT,
                                                    submission.getSubredditName());
                                            i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        } else {
                                            i = new Intent(contextActivity, Tumblr.class);
                                            i.putExtra(Tumblr.SUBREDDIT,
                                                    submission.getSubredditName());
                                            i.putExtra(Album.EXTRA_URL, submission.getUrl());
                                        }
                                        addAdaptorPosition(i, submission,
                                                holder.getAdapterPosition());
                                        contextActivity.startActivity(i);
                                        contextActivity.overridePendingTransition(R.anim.slideright,
                                                R.anim.fade_out);
                                    } else {
                                        LinkUtil.openExternally(submission.getUrl());

                                    }
                                    break;
                                case DEVIANTART:
                                case XKCD:
                                case IMAGE:
                                    openImage(type, contextActivity, submission, holder.leadImage,
                                            holder.getAdapterPosition());
                                    break;
                                case GIF:
                                    openGif(contextActivity, submission,
                                            holder.getAdapterPosition());
                                    break;
                                case NONE:
                                    if (holder != null) {
                                        holder.itemView.performClick();
                                    }
                                    break;
                                case VIDEO:
                                    if (!LinkUtil.tryOpenWithVideoPlugin(submission.getUrl())) {
                                        LinkUtil.openUrl(submission.getUrl(),
                                                Palette.getStatusBarColor(), contextActivity);
                                    }
                                    break;
                            }
                        } else {
                            LinkUtil.openExternally(submission.getUrl());
                        }
                    }
                } else {
                    if (!(contextActivity instanceof PeekViewActivity)
                            || !((PeekViewActivity) contextActivity).isPeeking()) {

                        Snackbar s = Snackbar.make(holder.itemView, R.string.go_online_view_content,
                                Snackbar.LENGTH_SHORT);
                        View view = s.getView();
                        TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        s.show();
                    }
                }
            }
        });
    }

    public static void openRedditContent(String url, Context c) {
        new OpenRedditLink(c, url);
    }

    public static void openImage(ContentType.Type type, Activity contextActivity,
            Submission submission, HeaderImageLinkView baseView, int adapterPosition) {
        if (SettingValues.image) {
            Intent myIntent = new Intent(contextActivity, MediaView.class);
            myIntent.putExtra(MediaView.SUBREDDIT, submission.getSubredditName());
            String url;
            String previewUrl;
            url = submission.getUrl();

            if (baseView != null
                    && baseView.lq
                    && SettingValues.loadImageLq
                    && type != ContentType.Type.XKCD) {
                myIntent.putExtra(MediaView.EXTRA_LQ, true);
                myIntent.putExtra(MediaView.EXTRA_DISPLAY_URL, baseView.loadedUrl);
            } else if (submission.getDataNode().has("preview")
                    && submission.getDataNode()
                    .get("preview")
                    .get("images")
                    .get(0)
                    .get("source")
                    .has("height")
                    && type
                    != ContentType.Type.XKCD) { //Load the preview image which has probably already been cached in memory instead of the direct link
                previewUrl = StringEscapeUtils.escapeHtml4(submission.getDataNode()
                        .get("preview")
                        .get("images")
                        .get(0)
                        .get("source")
                        .get("url")
                        .asText());
                if (baseView == null || (!SettingValues.loadImageLq && baseView.lq)) {
                    myIntent.putExtra(MediaView.EXTRA_DISPLAY_URL, previewUrl);
                } else {
                    myIntent.putExtra(MediaView.EXTRA_DISPLAY_URL, baseView.loadedUrl);
                }
            }
            myIntent.putExtra(MediaView.EXTRA_URL, url);
            addAdaptorPosition(myIntent, submission, adapterPosition);
            myIntent.putExtra(MediaView.EXTRA_SHARE_URL, submission.getUrl());

            contextActivity.startActivity(myIntent);

        } else {
            LinkUtil.openExternally(submission.getUrl());
        }

    }

    public static void addAdaptorPosition(Intent myIntent, Submission submission,
            int adapterPosition) {
        if (submission.getComments() == null && adapterPosition != -1) {
            myIntent.putExtra(MediaView.ADAPTER_POSITION, adapterPosition);
            myIntent.putExtra(MediaView.SUBMISSION_URL, submission.getPermalink());
        }
        SubmissionsView.currentPosition(adapterPosition);
        SubmissionsView.currentSubmission(submission);

    }

    public static void openGif(Activity contextActivity, Submission submission,
            int adapterPosition) {
        if (SettingValues.gif) {
            DataShare.sharedSubmission = submission;

            Intent myIntent = new Intent(contextActivity, MediaView.class);
            myIntent.putExtra(MediaView.SUBREDDIT, submission.getSubredditName());

            GifUtils.AsyncLoadGif.VideoType t =
                    GifUtils.AsyncLoadGif.getVideoType(submission.getUrl());

            if (t == GifUtils.AsyncLoadGif.VideoType.DIRECT && submission.getDataNode()
                    .has("preview") && submission.getDataNode()
                    .get("preview")
                    .get("images")
                    .get(0)
                    .has("variants") && submission.getDataNode()
                    .get("preview")
                    .get("images")
                    .get(0)
                    .get("variants")
                    .has("mp4")) {
                myIntent.putExtra(MediaView.EXTRA_URL, StringEscapeUtils.unescapeJson(
                        submission.getDataNode()
                                .get("preview")
                                .get("images")
                                .get(0)
                                .get("variants")
                                .get("mp4")
                                .get("source")
                                .get("url")
                                .asText()).replace("&amp;", "&"));
            } else if (t == GifUtils.AsyncLoadGif.VideoType.DIRECT
                    && submission.getDataNode()
                    .has("media")
                    && submission.getDataNode().get("media").has("reddit_video")
                    && submission.getDataNode()
                    .get("media")
                    .get("reddit_video")
                    .has("fallback_url")) {
                myIntent.putExtra(MediaView.EXTRA_URL, StringEscapeUtils.unescapeJson(
                        submission.getDataNode()
                                .get("media")
                                .get("reddit_video")
                                .get("fallback_url")
                                .asText()).replace("&amp;", "&"));

            } else {
                myIntent.putExtra(MediaView.EXTRA_URL, submission.getUrl());
            }
            if (submission.getDataNode().has("preview") && submission.getDataNode()
                    .get("preview")
                    .get("images")
                    .get(0)
                    .get("source")
                    .has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                String previewUrl = StringEscapeUtils.escapeHtml4(submission.getDataNode()
                        .get("preview")
                        .get("images")
                        .get(0)
                        .get("source")
                        .get("url")
                        .asText());
                myIntent.putExtra(MediaView.EXTRA_DISPLAY_URL, previewUrl);
            }
            addAdaptorPosition(myIntent, submission, adapterPosition);
            contextActivity.startActivity(myIntent);
        } else {
            LinkUtil.openExternally(submission.getUrl());
        }

    }

    public static int getCurrentTintColor(Context v) {
        return getStyleAttribColorValue(v, R.attr.tintColor, Color.WHITE);

    }

    public String reason;

    public String reportReason;

    boolean[] chosen    = new boolean[]{false, false, false};
    boolean[] oldChosen = new boolean[]{false, false, false};

    public static int getWhiteTintColor() {
        return Palette.ThemeEnum.DARK.getTint();
    }

    public <T extends Contribution> void showBottomSheet(final Activity mContext,
            final Submission submission, final NewsViewHolder holder, final List<T> posts,
            final String baseSub, final RecyclerView recyclerview, final boolean full) {

        int[] attrs = new int[]{R.attr.tintColor};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.profile, null);
        final Drawable sub =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sub, null);
        Drawable saved =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.iconstarfilled,
                        null);
        Drawable hide = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.hide, null);
        final Drawable report =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.report, null);
        Drawable copy =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_content_copy,
                        null);
        final Drawable readLater =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.save, null);
        Drawable open =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.openexternal, null);
        Drawable link = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.link, null);
        Drawable reddit =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.commentchange,
                        null);
        Drawable filter =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.filter, null);

        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        sub.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        saved.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        hide.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        report.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        link.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        reddit.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        readLater.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        filter.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();

        final BottomSheet.Builder b =
                new BottomSheet.Builder(mContext).title(Html.fromHtml(submission.getTitle()));


        final boolean isReadLater = mContext instanceof PostReadLater;
        final boolean isAddedToReadLaterList = ReadLater.isToBeReadLater(submission);
        if (Authentication.didOnline) {
            b.sheet(1, profile, "/u/" + submission.getAuthor())
                    .sheet(2, sub, "/r/" + submission.getSubredditName());
            String save = mContext.getString(R.string.btn_save);
            if (ActionStates.isSaved(submission)) {
                save = mContext.getString(R.string.comment_unsave);
            }
            if (Authentication.isLoggedIn) {
                b.sheet(3, saved, save);

            }
        }

        if (isAddedToReadLaterList) {
            b.sheet(28, readLater, "Mark As Read");
        } else {
            b.sheet(28, readLater, "Read later");
        }

        if (Authentication.didOnline) {
            if (Authentication.isLoggedIn) {
                b.sheet(12, report, mContext.getString(R.string.btn_report));
            }
        }

        if (submission.getSelftext() != null && !submission.getSelftext().isEmpty() && full) {
            b.sheet(25, copy, mContext.getString(R.string.submission_copy_text));
        }

        boolean hidden = submission.isHidden();
        if (!full && Authentication.didOnline) {
            if (!hidden) {
                b.sheet(5, hide, mContext.getString(R.string.submission_hide));
            } else {
                b.sheet(5, hide, mContext.getString(R.string.submission_unhide));
            }
        }
        b.sheet(7, open, mContext.getString(R.string.submission_link_extern));

        b.sheet(4, link, mContext.getString(R.string.submission_share_permalink))
                .sheet(8, reddit, mContext.getString(R.string.submission_share_reddit_url));
        if ((mContext instanceof MainActivity) || (mContext instanceof SubredditView)) {
            b.sheet(10, filter, mContext.getString(R.string.filter_content));
        }

        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 1: {
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                        mContext.startActivity(i);
                    }
                    break;
                    case 2: {
                        Intent i = new Intent(mContext, SubredditView.class);
                        i.putExtra(SubredditView.EXTRA_SUBREDDIT, submission.getSubredditName());
                        mContext.startActivityForResult(i, 14);
                    }
                    break;
                    case 10:


                        String[] choices;
                        final String flair = submission.getSubmissionFlair().getText() != null
                                ? submission.getSubmissionFlair().getText() : "";
                        if (flair.isEmpty()) {
                            choices = new String[]{
                                    mContext.getString(R.string.filter_posts_sub,
                                            submission.getSubredditName()),
                                    mContext.getString(R.string.filter_posts_user,
                                            submission.getAuthor()),
                                    mContext.getString(R.string.filter_posts_urls,
                                            submission.getDomain()),
                                    mContext.getString(R.string.filter_open_externally,
                                            submission.getDomain())
                            };

                            chosen = new boolean[]{
                                    Arrays.asList(SettingValues.subredditFilters.toLowerCase(
                                            Locale.ENGLISH).split(",")).contains(
                                            submission.getSubredditName()
                                                    .toLowerCase(Locale.ENGLISH)), Arrays.asList(
                                    SettingValues.userFilters.toLowerCase(Locale.ENGLISH)
                                            .split(",")).contains(
                                    submission.getAuthor().toLowerCase(Locale.ENGLISH)),
                                    Arrays.asList(
                                            SettingValues.domainFilters.toLowerCase(Locale.ENGLISH)
                                                    .split(",")).contains(
                                            submission.getDomain().toLowerCase(Locale.ENGLISH)),
                                    Arrays.asList(
                                            SettingValues.alwaysExternal.toLowerCase(Locale.ENGLISH)
                                                    .split(",")).contains(
                                            submission.getDomain().toLowerCase(Locale.ENGLISH))
                            };
                            oldChosen = chosen.clone();
                        } else {
                            choices = new String[]{
                                    mContext.getString(R.string.filter_posts_sub,
                                            submission.getSubredditName()),
                                    mContext.getString(R.string.filter_posts_user,
                                            submission.getAuthor()),
                                    mContext.getString(R.string.filter_posts_urls,
                                            submission.getDomain()),
                                    mContext.getString(R.string.filter_open_externally,
                                            submission.getDomain()),
                                    mContext.getString(R.string.filter_posts_flair, flair, baseSub)
                            };
                        }
                        chosen = new boolean[]{
                                Arrays.asList(
                                        SettingValues.subredditFilters.toLowerCase(Locale.ENGLISH)
                                                .split(",")).contains(
                                        submission.getSubredditName().toLowerCase(Locale.ENGLISH)),
                                Arrays.asList(SettingValues.userFilters.toLowerCase(Locale.ENGLISH)
                                        .split(",")).contains(
                                        submission.getAuthor().toLowerCase(Locale.ENGLISH)),
                                Arrays.asList(
                                        SettingValues.domainFilters.toLowerCase(Locale.ENGLISH)
                                                .split(",")).contains(
                                        submission.getDomain().toLowerCase(Locale.ENGLISH)),
                                Arrays.asList(
                                        SettingValues.alwaysExternal.toLowerCase(Locale.ENGLISH)
                                                .split(",")).contains(
                                        submission.getDomain().toLowerCase(Locale.ENGLISH)),
                                Arrays.asList(SettingValues.flairFilters.toLowerCase(Locale.ENGLISH)
                                        .split(",")).contains(baseSub + ":" + flair)
                        };
                        oldChosen = chosen.clone();

                        new AlertDialogWrapper.Builder(mContext).setTitle(R.string.filter_title)
                                .alwaysCallMultiChoiceCallback()
                                .setMultiChoiceItems(choices, chosen,
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which,
                                                    boolean isChecked) {
                                                chosen[which] = isChecked;
                                            }
                                        })
                                .setPositiveButton(R.string.filter_btn,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                boolean filtered = false;
                                                SharedPreferences.Editor e =
                                                        SettingValues.prefs.edit();
                                                if (chosen[0] && chosen[0] != oldChosen[0]) {
                                                    SettingValues.subredditFilters =
                                                            SettingValues.subredditFilters
                                                                    + (
                                                                    (SettingValues.subredditFilters.isEmpty()
                                                                            || SettingValues.subredditFilters
                                                                            .endsWith(",")) ? ""
                                                                            : ",")
                                                                    + submission.getSubredditName();
                                                    filtered = true;
                                                    e.putString(
                                                            SettingValues.PREF_SUBREDDIT_FILTERS,
                                                            SettingValues.subredditFilters);
                                                    PostMatch.subreddits = null;
                                                } else if (!chosen[0]
                                                        && chosen[0] != oldChosen[0]) {
                                                    SettingValues.subredditFilters =
                                                            SettingValues.subredditFilters.replace(
                                                                    submission.getSubredditName(),
                                                                    "");
                                                    filtered = false;
                                                    e.putString(
                                                            SettingValues.PREF_SUBREDDIT_FILTERS,
                                                            SettingValues.subredditFilters);
                                                    e.apply();
                                                    PostMatch.subreddits = null;
                                                }
                                                if (chosen[1] && chosen[1] != oldChosen[1]) {
                                                    SettingValues.userFilters =
                                                            SettingValues.userFilters + ((
                                                                    SettingValues.userFilters.isEmpty()
                                                                            || SettingValues.userFilters
                                                                            .endsWith(",")) ? ""
                                                                    : ",") + submission.getAuthor();
                                                    filtered = true;
                                                    e.putString(SettingValues.PREF_USER_FILTERS,
                                                            SettingValues.userFilters);
                                                    PostMatch.users = null;
                                                } else if (!chosen[1]
                                                        && chosen[1] != oldChosen[1]) {
                                                    SettingValues.userFilters =
                                                            SettingValues.userFilters.replace(
                                                                    submission.getAuthor(), "");
                                                    filtered = false;
                                                    e.putString(SettingValues.PREF_USER_FILTERS,
                                                            SettingValues.userFilters);
                                                    e.apply();
                                                    PostMatch.users = null;
                                                }
                                                if (chosen[2] && chosen[2] != oldChosen[2]) {
                                                    SettingValues.domainFilters =
                                                            SettingValues.domainFilters + ((
                                                                    SettingValues.domainFilters.isEmpty()
                                                                            || SettingValues.domainFilters
                                                                            .endsWith(",")) ? ""
                                                                    : ",") + submission.getDomain();
                                                    filtered = true;
                                                    e.putString(SettingValues.PREF_DOMAIN_FILTERS,
                                                            SettingValues.domainFilters);
                                                    PostMatch.domains = null;
                                                } else if (!chosen[2]
                                                        && chosen[2] != oldChosen[2]) {
                                                    SettingValues.domainFilters =
                                                            SettingValues.domainFilters.replace(
                                                                    submission.getDomain(), "");
                                                    filtered = false;
                                                    e.putString(SettingValues.PREF_DOMAIN_FILTERS,
                                                            SettingValues.domainFilters);
                                                    e.apply();
                                                    PostMatch.domains = null;
                                                }
                                                if (chosen[3] && chosen[3] != oldChosen[3]) {
                                                    SettingValues.alwaysExternal =
                                                            SettingValues.alwaysExternal + ((
                                                                    SettingValues.alwaysExternal.isEmpty()
                                                                            || SettingValues.alwaysExternal
                                                                            .endsWith(",")) ? ""
                                                                    : ",") + submission.getDomain();
                                                    e.putString(SettingValues.PREF_ALWAYS_EXTERNAL,
                                                            SettingValues.alwaysExternal);
                                                    e.apply();
                                                } else if (!chosen[3]
                                                        && chosen[3] != oldChosen[3]) {
                                                    SettingValues.alwaysExternal =
                                                            SettingValues.alwaysExternal.replace(
                                                                    submission.getDomain(), "");
                                                    e.putString(SettingValues.PREF_ALWAYS_EXTERNAL,
                                                            SettingValues.alwaysExternal);
                                                    e.apply();
                                                }
                                                if (chosen.length > 4) {
                                                    if (chosen[4] && chosen[4] != oldChosen[4]) {
                                                        SettingValues.flairFilters =
                                                                SettingValues.flairFilters + ((
                                                                        SettingValues.flairFilters.isEmpty()
                                                                                || SettingValues.flairFilters
                                                                                .endsWith(",")) ? ""
                                                                        : ",") + (baseSub
                                                                        + ":"
                                                                        + flair);
                                                        e.putString(
                                                                SettingValues.PREF_FLAIR_FILTERS,
                                                                SettingValues.flairFilters);
                                                        e.apply();
                                                        PostMatch.flairs = null;
                                                        filtered = true;
                                                    } else if (!chosen[4]
                                                            && chosen[4] != oldChosen[4]) {
                                                        SettingValues.flairFilters =
                                                                SettingValues.flairFilters.toLowerCase(
                                                                        Locale.ENGLISH)
                                                                        .replace((baseSub
                                                                                        + ":"
                                                                                        + flair).toLowerCase(
                                                                                Locale.ENGLISH),
                                                                                "");
                                                        e.putString(
                                                                SettingValues.PREF_FLAIR_FILTERS,
                                                                SettingValues.flairFilters);
                                                        e.apply();
                                                        PostMatch.flairs = null;
                                                    }
                                                }
                                                if (filtered) {
                                                    e.apply();
                                                    PostMatch.domains = null;
                                                    PostMatch.subreddits = null;
                                                    PostMatch.users = null;
                                                    ArrayList<Contribution> toRemove =
                                                            new ArrayList<>();
                                                    for (Contribution s : posts) {
                                                        if (s instanceof Submission
                                                                && PostMatch.doesMatch(
                                                                (Submission) s)) {
                                                            toRemove.add(s);
                                                        }
                                                    }

                                                    OfflineSubreddit s =
                                                            OfflineSubreddit.getSubreddit(baseSub,
                                                                    false, mContext);

                                                    for (Contribution remove : toRemove) {
                                                        final int pos = posts.indexOf(remove);
                                                        posts.remove(pos);
                                                        if (baseSub != null) {
                                                            s.hideMulti(pos);
                                                        }
                                                    }
                                                    s.writeToMemoryNoStorage();
                                                    recyclerview.getAdapter()
                                                            .notifyDataSetChanged();
                                                }
                                            }
                                        })
                                .setNegativeButton(R.string.btn_cancel, null)
                                .show();
                        break;

                    case 5: {
                        hideSubmission(submission, posts, baseSub, recyclerview, mContext);
                    }
                    break;
                    case 7:
                        LinkUtil.openExternally(submission.getUrl());
                        if (submission.isNsfw() && !SettingValues.storeNSFWHistory) {
                            //Do nothing if the post is NSFW and storeNSFWHistory is not enabled
                        } else if (SettingValues.storeHistory) {
                            HasSeen.addSeen(submission.getFullName());
                        }
                        break;
                    case 28:
                        if (!isAddedToReadLaterList) {
                            ReadLater.setReadLater(submission, true);
                            Snackbar s = Snackbar.make(holder.itemView, "Added to read later!",
                                    Snackbar.LENGTH_SHORT);
                            View view = s.getView();
                            TextView tv =
                                    view.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.setAction(R.string.btn_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ReadLater.setReadLater(submission, false);
                                    Snackbar s2 = Snackbar.make(holder.itemView,
                                            "Removed from read later", Snackbar.LENGTH_SHORT);
                                    View view2 = s2.getView();
                                    TextView tv2 = view2.findViewById(
                                            android.support.design.R.id.snackbar_text);
                                    tv2.setTextColor(Color.WHITE);
                                    s2.show();
                                }
                            });
                            if (NetworkUtil.isConnected(mContext)) {
                                new CommentCacheAsync(Arrays.asList(submission), mContext,
                                        CommentCacheAsync.SAVED_SUBMISSIONS,
                                        new boolean[]{true, true}).executeOnExecutor(
                                        AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                            s.show();
                        } else {
                            ReadLater.setReadLater(submission, false);
                            if (isReadLater || !Authentication.didOnline) {
                                final int pos = posts.indexOf(submission);
                                posts.remove(submission);

                                recyclerview.getAdapter()
                                        .notifyItemRemoved(holder.getAdapterPosition());

                                Snackbar s2 =
                                        Snackbar.make(holder.itemView, "Removed from read later",
                                                Snackbar.LENGTH_SHORT);
                                View view2 = s2.getView();
                                TextView tv2 = view2.findViewById(
                                        android.support.design.R.id.snackbar_text);
                                tv2.setTextColor(Color.WHITE);
                                s2.setAction(R.string.btn_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        posts.add(pos, (T) submission);
                                        recyclerview.getAdapter().notifyDataSetChanged();
                                    }
                                });
                            } else {
                                Snackbar s2 =
                                        Snackbar.make(holder.itemView, "Removed from read later",
                                                Snackbar.LENGTH_SHORT);
                                View view2 = s2.getView();
                                TextView tv2 = view2.findViewById(
                                        android.support.design.R.id.snackbar_text);
                                s2.show();
                            }
                            OfflineSubreddit.newSubreddit(CommentCacheAsync.SAVED_SUBMISSIONS)
                                    .deleteFromMemory(submission.getFullName());

                        }
                        break;
                    case 4:
                        Reddit.defaultShareText(Html.fromHtml(submission.getTitle()).toString(),
                                StringEscapeUtils.escapeHtml4(submission.getUrl()), mContext);
                        break;
                    case 12:
                        reportReason = "";
                        new MaterialDialog.Builder(mContext).input(
                                mContext.getString(R.string.input_reason_for_report), null, true,
                                new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        reportReason = input.toString();
                                    }
                                })
                                .title(R.string.report_post)
                                .alwaysCallInputCallback()
                                .inputType(InputType.TYPE_CLASS_TEXT
                                        | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
                                        | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                                .positiveText(R.string.btn_report)
                                .negativeText(R.string.btn_cancel)
                                .onNegative(null)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                        new AsyncTask<Void, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                try {
                                                    new AccountManager(
                                                            Authentication.reddit).report(
                                                            submission, reportReason);
                                                } catch (ApiException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(Void aVoid) {
                                                if (holder.itemView != null) {
                                                    try {
                                                        Snackbar s = Snackbar.make(holder.itemView,
                                                                R.string.msg_report_sent,
                                                                Snackbar.LENGTH_SHORT);
                                                        View view = s.getView();
                                                        TextView tv = view.findViewById(
                                                                android.support.design.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        s.show();
                                                    } catch (Exception ignored) {

                                                    }
                                                }
                                            }
                                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    }
                                })
                                .show();

                        break;
                    case 8:
                        Reddit.defaultShareText(Html.fromHtml(submission.getTitle()).toString(),
                                "https://reddit.com" + submission.getPermalink(), mContext);
                        break;
                    case 6: {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(
                                Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Link", submission.getUrl());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, R.string.submission_link_copied,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                    case 25:
                        final TextView showText = new TextView(mContext);
                        showText.setText(StringEscapeUtils.unescapeHtml4(
                                submission.getTitle() + "\n\n" + submission.getSelftext()));
                        showText.setTextIsSelectable(true);
                        int sixteen = Reddit.dpToPxVertical(24);
                        showText.setPadding(sixteen, 0, sixteen, 0);
                        AlertDialogWrapper.Builder builder =
                                new AlertDialogWrapper.Builder(mContext);
                        builder.setView(showText)
                                .setTitle("Select text to copy")
                                .setCancelable(true)
                                .setPositiveButton("COPY SELECTED",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String selected = showText.getText()
                                                        .toString()
                                                        .substring(showText.getSelectionStart(),
                                                                showText.getSelectionEnd());
                                                if (!selected.isEmpty()) {
                                                    ClipboardManager clipboard =
                                                            (ClipboardManager) mContext.getSystemService(
                                                                    Context.CLIPBOARD_SERVICE);
                                                    ClipData clip =
                                                            ClipData.newPlainText("Selftext",
                                                                    selected);
                                                    clipboard.setPrimaryClip(clip);

                                                    Toast.makeText(mContext,
                                                            R.string.submission_comment_copied,
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    ClipboardManager clipboard =
                                                            (ClipboardManager) mContext.getSystemService(
                                                                    Context.CLIPBOARD_SERVICE);
                                                    ClipData clip =
                                                            ClipData.newPlainText("Selftext",
                                                                    Html.fromHtml(
                                                                            submission.getTitle()
                                                                                    + "\n\n"
                                                                                    + submission.getSelftext()));
                                                    clipboard.setPrimaryClip(clip);

                                                    Toast.makeText(mContext,
                                                            R.string.submission_comment_copied,
                                                            Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        })
                                .setNegativeButton(R.string.btn_cancel, null)
                                .setNeutralButton("COPY ALL",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ClipboardManager clipboard =
                                                        (ClipboardManager) mContext.getSystemService(
                                                                Context.CLIPBOARD_SERVICE);
                                                ClipData clip = ClipData.newPlainText("Selftext",
                                                        Html.fromHtml(submission.getTitle()
                                                                + "\n\n"
                                                                + submission.getSelftext()));
                                                clipboard.setPrimaryClip(clip);

                                                Toast.makeText(mContext,
                                                        R.string.submission_comment_copied,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                .show();
                        break;
                }
            }
        });
        b.show();
    }

    public <T extends Contribution> void hideSubmission(final Submission submission,
            final List<T> posts, final String baseSub, final RecyclerView recyclerview, Context c) {
        final int pos = posts.indexOf(submission);
        if (pos != -1) {
            if (submission.isHidden()) {
                posts.remove(pos);
                Hidden.undoHidden(submission);
                recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                Snackbar snack = Snackbar.make(recyclerview, R.string.submission_info_unhidden,
                        Snackbar.LENGTH_LONG);
                View view = snack.getView();
                TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.show();
            } else {
                final T t = posts.get(pos);
                posts.remove(pos);
                Hidden.setHidden(t);
                final OfflineSubreddit s;
                boolean success = false;
                if (baseSub != null) {
                    s = OfflineSubreddit.getSubreddit(baseSub, false, c);
                    try {
                        s.hide(pos);
                        success = true;
                    } catch (Exception e) {
                    }
                } else {
                    success = false;
                    s = null;
                }

                recyclerview.getAdapter().notifyItemRemoved(pos + 1);

                final boolean finalSuccess = success;
                Snackbar snack = Snackbar.make(recyclerview, R.string.submission_info_hidden,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.btn_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (baseSub != null && s != null && finalSuccess) {
                                    s.unhideLast();
                                }
                                posts.add(pos, t);
                                recyclerview.getAdapter().notifyItemInserted(pos + 1);
                                Hidden.undoHidden(t);

                            }
                        });
                View view = snack.getView();
                TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.show();
            }

        }
    }

    public void doText(NewsViewHolder holder, Submission submission, Context mContext,
            String baseSub) {
        SpannableStringBuilder t = SubmissionCache.getTitleLine(submission, mContext);
        SpannableStringBuilder l = SubmissionCache.getInfoLine(submission, mContext, baseSub);

        int[] textSizeAttr = new int[]{R.attr.font_cardtitle, R.attr.font_cardinfo};
        TypedArray a = mContext.obtainStyledAttributes(textSizeAttr);
        int textSizeT = a.getDimensionPixelSize(0, 18);
        int textSizeI = a.getDimensionPixelSize(1, 14);

        a.recycle();

        t.setSpan(new AbsoluteSizeSpan(textSizeT), 0, t.length(), 0);
        l.setSpan(new AbsoluteSizeSpan(textSizeI), 0, l.length(), 0);

        SpannableStringBuilder s = new SpannableStringBuilder();
        if (SettingValues.titleTop) {
            s.append(t);
            s.append("\n");
            s.append(l);
        } else {
            s.append(l);
            s.append("\n");
            s.append(t);
        }

        holder.title.setText(s);

    }

    public <T extends Contribution> void populateNewsViewHolder(final NewsViewHolder holder,
            final Submission submission, final Activity mContext, boolean fullscreen,
            final boolean full, final List<T> posts, final RecyclerView recyclerview,
            final boolean same, final boolean offline, final String baseSub,
            @Nullable final CommentAdapter adapter) {

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheet(mContext, submission, holder, posts, baseSub, recyclerview, full);
            }
        });

        //Use this to offset the submission score
        int submissionScore = submission.getScore();

        final int commentCount = submission.getCommentCount();
        final int more = LastComments.commentsSince(submission);
        String scoreRatio =
                (SettingValues.upvotePercentage && full && submission.getUpvoteRatio() != null) ?
                        "("
                                + (int) (submission.getUpvoteRatio() * 100)
                                + "%)" : "";

        if (!scoreRatio.isEmpty()) {
            TextView percent = holder.itemView.findViewById(R.id.percent);
            percent.setVisibility(View.VISIBLE);
            percent.setText(scoreRatio);

            final double numb = (submission.getUpvoteRatio());
            if (numb <= .5) {
                if (numb <= .1) {
                    percent.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
                } else if (numb <= .3) {
                    percent.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_400));
                } else {
                    percent.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_300));
                }
            } else {
                if (numb >= .9) {
                    percent.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                } else if (numb >= .7) {
                    percent.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_400));
                } else {
                    percent.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_300));
                }
            }
        }

        //Save the score so we can use it in the OnClickListeners for the vote buttons

        ImageView thumbImage2 = holder.thumbnail;

        if (holder.leadImage.thumbImage2 == null) {
            holder.leadImage.setThumbnail(thumbImage2);
        }

        final ContentType.Type type = ContentType.getContentType(submission);

        addClickFunctions(holder.itemView, type, mContext, submission, holder, full);

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenRedditLink.openUrl(mContext, submission.getPermalink(), true);
            }
        });

        if (thumbImage2 != null) {
            addClickFunctions(thumbImage2, type, mContext, submission, holder, full);
        }

        holder.leadImage.setSubmissionNews(submission, full, baseSub, type);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (offline) {
                    Snackbar s =
                            Snackbar.make(holder.itemView, mContext.getString(R.string.offline_msg),
                                    Snackbar.LENGTH_SHORT);
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    if (SettingValues.actionbarTap && !full) {
                        CreateCardView.toggleActionbar(holder.itemView);
                    } else {
                        holder.itemView.findViewById(R.id.menu).callOnClick();
                    }
                }
                return true;
            }

        });

        doText(holder, submission, mContext, baseSub);

        if (HasSeen.getSeen(submission) && !full) {
            holder.title.setAlpha(0.54f);
        } else {
            holder.title.setAlpha(1f);
        }
    }
}