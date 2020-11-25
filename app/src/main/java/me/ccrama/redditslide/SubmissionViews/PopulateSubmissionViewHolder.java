package me.ccrama.redditslide.SubmissionViews;


import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.RobotoTypefaces;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.ApiException;
import net.dean.jraw.fluent.FlairReference;
import net.dean.jraw.fluent.FluentRedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.InvalidScopeException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.Ruleset;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditRule;
import net.dean.jraw.models.Thing;
import net.dean.jraw.models.VoteDirection;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.GalleryImage;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.PostReadLater;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Reauthenticate;
import me.ccrama.redditslide.Activities.RedditGallery;
import me.ccrama.redditslide.Activities.RedditGalleryPager;
import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Tumblr;
import me.ccrama.redditslide.Activities.TumblrPager;
import me.ccrama.redditslide.Adapters.CommentAdapter;
import me.ccrama.redditslide.Adapters.SubmissionViewHolder;
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
import me.ccrama.redditslide.Toolbox.ToolboxUI;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.AnimateHelper;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.Vote;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SubmissionParser;

import static me.ccrama.redditslide.Notifications.ImageDownloadNotificationService.EXTRA_SUBMISSION_TITLE;

/**
 * Created by ccrama on 9/19/2015.
 */
public class PopulateSubmissionViewHolder {

    public PopulateSubmissionViewHolder() {
    }

    public static int getStyleAttribColorValue(final Context context, final int attribResId,
            final int defaultValue) {
        final TypedValue tv = new TypedValue();
        final boolean found = context.getTheme().resolveAttribute(attribResId, tv, true);
        return found ? tv.data : defaultValue;
    }

    private static void addClickFunctions(final View base, final ContentType.Type type,
            final Activity contextActivity, final Submission submission,
            final SubmissionViewHolder holder, final boolean full) {
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
                                holder.body.setAlpha(0.54f);
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
                                case STREAMABLE:
                                    if (SettingValues.video) {
                                        Intent myIntent =
                                                new Intent(contextActivity, MediaView.class);
                                        myIntent.putExtra(MediaView.SUBREDDIT,
                                                submission.getSubredditName());
                                        myIntent.putExtra(MediaView.EXTRA_URL, submission.getUrl());
                                        myIntent.putExtra(EXTRA_SUBMISSION_TITLE, submission.getTitle());
                                        addAdaptorPosition(myIntent, submission,
                                                holder.getAdapterPosition());
                                        contextActivity.startActivity(myIntent);
                                    } else {
                                        LinkUtil.openExternally(submission.getUrl());
                                    }
                                    break;
                                case IMGUR:
                                case DEVIANTART:
                                case XKCD:
                                case IMAGE:
                                    openImage(type, contextActivity, submission, holder.leadImage,
                                            holder.getAdapterPosition());
                                    break;
                                case EMBEDDED:
                                    if (SettingValues.video) {
                                        String data = HtmlCompat.fromHtml(submission.getDataNode()
                                                .get("media_embed")
                                                .get("content")
                                                .asText(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
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
                                case REDDIT_GALLERY:
                                    if (SettingValues.album) {
                                        Intent i;
                                        if (SettingValues.albumSwipe) {
                                            i = new Intent(contextActivity, RedditGalleryPager.class);
                                            i.putExtra(AlbumPager.SUBREDDIT,
                                                    submission.getSubredditName());
                                            i.putExtra(EXTRA_SUBMISSION_TITLE, submission.getTitle());
                                        } else {
                                            i = new Intent(contextActivity, RedditGallery.class);
                                            i.putExtra(Album.SUBREDDIT,
                                                    submission.getSubredditName());
                                            i.putExtra(EXTRA_SUBMISSION_TITLE, submission.getTitle());
                                        }

                                        i.putExtra(RedditGallery.SUBREDDIT,
                                                submission.getSubredditName());

                                        ArrayList<GalleryImage> urls = new ArrayList<>();

                                        JsonNode dataNode = submission.getDataNode();
                                        if (dataNode.has("gallery_data")) {
                                            for (JsonNode identifier : dataNode.get("gallery_data").get("items")) {
                                                if (dataNode.has("media_metadata") && dataNode.get(
                                                        "media_metadata")
                                                        .has(identifier.get("media_id").asText())) {
                                                    urls.add(new GalleryImage(dataNode.get("media_metadata")
                                                            .get(identifier.get("media_id").asText())
                                                            .get("s")));
                                                }
                                            }
                                        } else if (dataNode.has("crosspost_parent_list")) { //Else, try getting crosspost gallery data
                                            JsonNode crosspost_parent = dataNode.get("crosspost_parent_list").get(0);
                                            for (JsonNode identifier : crosspost_parent.get("gallery_data").get("items")) {
                                                if (crosspost_parent.has("media_metadata") && crosspost_parent.get(
                                                        "media_metadata")
                                                        .has(identifier.get("media_id").asText())) {
                                                    urls.add(new GalleryImage(crosspost_parent.get("media_metadata")
                                                            .get(identifier.get("media_id").asText())
                                                            .get("s")));
                                                }
                                            }
                                        }

                                        Bundle urlsBundle = new Bundle();
                                        urlsBundle.putSerializable(RedditGallery.GALLERY_URLS, urls);
                                        i.putExtras(urlsBundle);

                                        addAdaptorPosition(i, submission,
                                                holder.getAdapterPosition());
                                        contextActivity.startActivity(i);
                                        contextActivity.overridePendingTransition(R.anim.slideright,
                                                R.anim.fade_out);
                                    } else {
                                        LinkUtil.openExternally(submission.getUrl());
                                    }
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
                                            i.putExtra(EXTRA_SUBMISSION_TITLE, submission.getTitle());
                                        } else {
                                            i = new Intent(contextActivity, Album.class);
                                            i.putExtra(Album.SUBREDDIT,
                                                    submission.getSubredditName());
                                            i.putExtra(EXTRA_SUBMISSION_TITLE, submission.getTitle());
                                        }
                                        i.putExtra(Album.EXTRA_URL, submission.getUrl());

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
                                        } else {
                                            i = new Intent(contextActivity, Tumblr.class);
                                            i.putExtra(Tumblr.SUBREDDIT,
                                                    submission.getSubredditName());
                                        }
                                        i.putExtra(Album.EXTRA_URL, submission.getUrl());

                                        addAdaptorPosition(i, submission,
                                                holder.getAdapterPosition());
                                        contextActivity.startActivity(i);
                                        contextActivity.overridePendingTransition(R.anim.slideright,
                                                R.anim.fade_out);
                                    } else {
                                        LinkUtil.openExternally(submission.getUrl());

                                    }
                                    break;
                                case VREDDIT_REDIRECT:
                                case GIF:
                                case VREDDIT_DIRECT:
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
                        TextView tv = view.findViewById(
                                com.google.android.material.R.id.snackbar_text);
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
            myIntent.putExtra(EXTRA_SUBMISSION_TITLE, submission.getTitle());
            String previewUrl;
            String url = submission.getUrl();

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
                previewUrl = submission.getDataNode()
                        .get("preview")
                        .get("images")
                        .get(0)
                        .get("source")
                        .get("url")
                        .asText();
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
            myIntent.putExtra(EXTRA_SUBMISSION_TITLE, submission.getTitle());

            GifUtils.AsyncLoadGif.VideoType t =
                    GifUtils.AsyncLoadGif.getVideoType(submission.getUrl());

            if (t == GifUtils.AsyncLoadGif.VideoType.VREDDIT) {
                if (submission.getDataNode().has("media") && submission.getDataNode()
                        .get("media")
                        .has("reddit_video") && submission.getDataNode()
                        .get("media")
                        .get("reddit_video").has("hls_url")) {
                    myIntent.putExtra(MediaView.EXTRA_URL, StringEscapeUtils.unescapeJson(submission
                            .getDataNode()
                            .get("media")
                            .get("reddit_video")
                            .get("dash_url") //In the future, we could load the HLS url as well
                            .asText()).replace("&amp;", "&"));
                } else if (submission.getDataNode().has("media") && submission.getDataNode()
                            .get("media")
                            .has("reddit_video")) {
                        myIntent.putExtra(MediaView.EXTRA_URL, StringEscapeUtils.unescapeJson(submission
                                .getDataNode()
                                .get("media")
                                .get("reddit_video")
                                .get("fallback_url")
                                .asText()).replace("&amp;", "&"));
                } else if (submission.getDataNode().has("crosspost_parent_list")) {
                    myIntent.putExtra(MediaView.EXTRA_URL, StringEscapeUtils.unescapeJson(submission
                            .getDataNode()
                            .get("crosspost_parent_list")
                            .get(0)
                            .get("media")
                            .get("reddit_video")
                            .get("dash_url")
                            .asText()).replace("&amp;", "&"));
                } else {
                    new OpenVRedditTask(contextActivity, submission.getSubredditName()).executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR, submission.getUrl());
                    return;

                }

            } else if (t.shouldLoadPreview()
                    && submission.getDataNode().has("preview")
                    && submission.getDataNode().get("preview").get("images").get(0).has("variants")
                    && submission.getDataNode()
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
            } else if (t.shouldLoadPreview()
                    && submission.getDataNode().has("preview")
                    && submission.getDataNode().get("preview").get("reddit_video_preview").has("fallback_url")) {
                myIntent.putExtra(MediaView.EXTRA_URL, StringEscapeUtils.unescapeJson(
                        submission.getDataNode()
                                .get("preview")
                                .get("reddit_video_preview")
                                .get("fallback_url")
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

            } else if (t != GifUtils.AsyncLoadGif.VideoType.OTHER) {
                myIntent.putExtra(MediaView.EXTRA_URL, submission.getUrl());
            } else {
                LinkUtil.openUrl(submission.getUrl(),
                        Palette.getColor(submission.getSubredditName()), contextActivity,
                        adapterPosition, submission);
                return;
            }
            if (submission.getDataNode().has("preview") && submission.getDataNode()
                    .get("preview")
                    .get("images")
                    .get(0)
                    .get("source")
                    .has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                String previewUrl = submission.getDataNode()
                        .get("preview")
                        .get("images")
                        .get(0)
                        .get("source")
                        .get("url")
                        .asText();
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

    boolean[] chosen    = new boolean[]{false, false, false};
    boolean[] oldChosen = new boolean[]{false, false, false};

    public static int getWhiteTintColor() {
        return Palette.ThemeEnum.DARK.getTint();
    }

    public <T extends Contribution> void showBottomSheet(final Activity mContext,
            final Submission submission, final SubmissionViewHolder holder, final List<T> posts,
            final String baseSub, final RecyclerView recyclerview, final boolean full) {

        int[] attrs = new int[]{R.attr.tintColor};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.profile, null);
        final Drawable sub =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sub, null);
        Drawable saved =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.star,
                        null);
        Drawable hide = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.hide, null);
        final Drawable report =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.report, null);
        Drawable copy =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.copy,
                        null);
        final Drawable readLater =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.save, null);
        Drawable open =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.open_external, null);
        Drawable link = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.link, null);
        Drawable reddit =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.commentchange,
                        null);
        Drawable filter =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.filter, null);
        Drawable crosspost =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.forward, null);

        profile.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        sub.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        saved.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        hide.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        report.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        copy.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        open.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        link.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        reddit.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        readLater.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        filter.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        crosspost.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));

        ta.recycle();

        final BottomSheet.Builder b =
                new BottomSheet.Builder(mContext).title(HtmlCompat.fromHtml(submission.getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));


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
                b.sheet(13, crosspost, mContext.getString(R.string.btn_crosspost));
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

                            chosen = new boolean[] {
                                    SettingValues.subredditFilters.contains(
                                            submission.getSubredditName().toLowerCase(Locale.ENGLISH)),
                                    SettingValues.userFilters.contains(
                                            submission.getAuthor().toLowerCase(Locale.ENGLISH)),
                                    SettingValues.domainFilters.contains(
                                            submission.getDomain().toLowerCase(Locale.ENGLISH)),
                                    SettingValues.alwaysExternal.contains(
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
                        chosen = new boolean[] {
                                SettingValues.subredditFilters.contains(
                                        submission.getSubredditName().toLowerCase(Locale.ENGLISH)),
                                SettingValues.userFilters.contains(
                                submission.getAuthor().toLowerCase(Locale.ENGLISH)),
                                SettingValues.domainFilters.contains(
                                        submission.getDomain().toLowerCase(Locale.ENGLISH)),
                                SettingValues.alwaysExternal.contains(
                                        submission.getDomain().toLowerCase(Locale.ENGLISH)),
                                SettingValues.flairFilters.contains(baseSub + ":" + flair.toLowerCase(Locale.ENGLISH).trim())
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
                                                SharedPreferences.Editor e = SettingValues.prefs.edit();
                                                if (chosen[0] && chosen[0] != oldChosen[0]) {
                                                    SettingValues.subredditFilters.add(submission.getSubredditName()
                                                            .toLowerCase(Locale.ENGLISH).trim());
                                                    filtered = true;
                                                    e.putStringSet(
                                                            SettingValues.PREF_SUBREDDIT_FILTERS,
                                                            SettingValues.subredditFilters);
                                                } else if (!chosen[0] && chosen[0] != oldChosen[0]) {
                                                    SettingValues.subredditFilters.remove(submission.getSubredditName()
                                                            .toLowerCase(Locale.ENGLISH).trim());
                                                    filtered = false;
                                                    e.putStringSet(
                                                            SettingValues.PREF_SUBREDDIT_FILTERS,
                                                            SettingValues.subredditFilters);
                                                    e.apply();
                                                }
                                                if (chosen[1] && chosen[1] != oldChosen[1]) {
                                                    SettingValues.userFilters.add(submission.getAuthor()
                                                            .toLowerCase(Locale.ENGLISH).trim());
                                                    filtered = true;
                                                    e.putStringSet(SettingValues.PREF_USER_FILTERS,
                                                            SettingValues.userFilters);
                                                } else if (!chosen[1] && chosen[1] != oldChosen[1]) {
                                                    SettingValues.userFilters.remove(submission.getAuthor()
                                                            .toLowerCase(Locale.ENGLISH).trim());
                                                    filtered = false;
                                                    e.putStringSet(SettingValues.PREF_USER_FILTERS,
                                                            SettingValues.userFilters);
                                                    e.apply();
                                                }
                                                if (chosen[2] && chosen[2] != oldChosen[2]) {
                                                    SettingValues.domainFilters.add(submission.getDomain()
                                                            .toLowerCase(Locale.ENGLISH).trim());
                                                    filtered = true;
                                                    e.putStringSet(SettingValues.PREF_DOMAIN_FILTERS,
                                                            SettingValues.domainFilters);
                                                } else if (!chosen[2] && chosen[2] != oldChosen[2]) {
                                                    SettingValues.domainFilters.remove(submission.getDomain()
                                                            .toLowerCase(Locale.ENGLISH).trim());
                                                    filtered = false;
                                                    e.putStringSet(SettingValues.PREF_DOMAIN_FILTERS,
                                                            SettingValues.domainFilters);
                                                    e.apply();
                                                }
                                                if (chosen[3] && chosen[3] != oldChosen[3]) {
                                                    SettingValues.alwaysExternal.add(submission.getDomain()
                                                            .toLowerCase(Locale.ENGLISH).trim());
                                                    e.putStringSet(SettingValues.PREF_ALWAYS_EXTERNAL,
                                                            SettingValues.alwaysExternal);
                                                    e.apply();
                                                } else if (!chosen[3] && chosen[3] != oldChosen[3]) {
                                                    SettingValues.alwaysExternal.remove(submission.getDomain()
                                                            .toLowerCase(Locale.ENGLISH).trim());
                                                    e.putStringSet(SettingValues.PREF_ALWAYS_EXTERNAL,
                                                            SettingValues.alwaysExternal);
                                                    e.apply();
                                                }
                                                if (chosen.length > 4) {
                                                    String s = (baseSub + ":" + flair)
                                                            .toLowerCase(Locale.ENGLISH).trim();
                                                    if (chosen[4] && chosen[4] != oldChosen[4]) {
                                                        SettingValues.flairFilters.add(s);
                                                        e.putStringSet(
                                                                SettingValues.PREF_FLAIR_FILTERS,
                                                                SettingValues.flairFilters);
                                                        e.apply();
                                                        filtered = true;
                                                    } else if (!chosen[4] && chosen[4] != oldChosen[4]) {
                                                        SettingValues.flairFilters.remove(s);
                                                        e.putStringSet(
                                                                SettingValues.PREF_FLAIR_FILTERS,
                                                                SettingValues.flairFilters);
                                                        e.apply();
                                                    }
                                                }
                                                if (filtered) {
                                                    e.apply();
                                                    ArrayList<Contribution> toRemove = new ArrayList<>();
                                                    for (Contribution s : posts) {
                                                        if (s instanceof Submission
                                                                && PostMatch.doesMatch((Submission) s)) {
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

                    case 3:
                        saveSubmission(submission, mContext, holder, full);
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
                    case 13:
                        LinkUtil.crosspost(submission, mContext);
                        break;
                    case 28:
                        if (!isAddedToReadLaterList) {
                            ReadLater.setReadLater(submission, true);
                            Snackbar s = Snackbar.make(holder.itemView, "Added to read later!",
                                    Snackbar.LENGTH_SHORT);
                            View view = s.getView();
                            TextView tv = view.findViewById(
                                    com.google.android.material.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.setAction(R.string.btn_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ReadLater.setReadLater(submission, false);
                                    Snackbar s2 = Snackbar.make(holder.itemView,
                                            "Removed from read later", Snackbar.LENGTH_SHORT);
                                    View view2 = s2.getView();
                                    TextView tv2 = view2.findViewById(
                                            com.google.android.material.R.id.snackbar_text);
                                    tv2.setTextColor(Color.WHITE);
                                    s2.show();
                                }
                            });
                            if (NetworkUtil.isConnected(mContext)) {
                                new CommentCacheAsync(Collections.singletonList(submission), mContext,
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
                                        com.google.android.material.R.id.snackbar_text);
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
                                        com.google.android.material.R.id.snackbar_text);
                                s2.show();
                            }
                            OfflineSubreddit.newSubreddit(CommentCacheAsync.SAVED_SUBMISSIONS)
                                    .deleteFromMemory(submission.getFullName());

                        }
                        break;
                    case 4:
                        Reddit.defaultShareText(HtmlCompat.fromHtml(submission.getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                                StringEscapeUtils.escapeHtml4(submission.getUrl()), mContext);
                        break;
                    case 12:
                        final MaterialDialog reportDialog = new MaterialDialog.Builder(mContext)
                                .customView(R.layout.report_dialog, true)
                                .title(R.string.report_post)
                                .positiveText(R.string.btn_report)
                                .negativeText(R.string.btn_cancel)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                        RadioGroup reasonGroup = dialog.getCustomView()
                                                .findViewById(R.id.report_reasons);
                                        String reportReason;
                                        if (reasonGroup.getCheckedRadioButtonId() == R.id.report_other) {
                                            reportReason = ((EditText) dialog.getCustomView()
                                                    .findViewById(R.id.input_report_reason)).getText().toString();
                                        } else {
                                            reportReason = ((RadioButton) reasonGroup
                                                    .findViewById(reasonGroup.getCheckedRadioButtonId()))
                                                    .getText().toString();
                                        }
                                        new AsyncReportTask(submission, holder.itemView)
                                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, reportReason);
                                    }
                                }).build();

                        final RadioGroup reasonGroup = reportDialog.getCustomView().findViewById(R.id.report_reasons);

                        reasonGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                if (checkedId == R.id.report_other)
                                    reportDialog.getCustomView().findViewById(R.id.input_report_reason)
                                            .setVisibility(View.VISIBLE);
                                else
                                    reportDialog.getCustomView().findViewById(R.id.input_report_reason)
                                            .setVisibility(View.GONE);
                            }
                        });

                        // Load sub's report reasons and show the appropriate ones
                        new AsyncTask<Void, Void, Ruleset>() {
                            @Override
                            protected Ruleset doInBackground(Void... voids) {
                                return Authentication.reddit.getRules(submission.getSubredditName());
                            }

                            @Override
                            protected void onPostExecute(Ruleset rules) {
                                reportDialog.getCustomView().findViewById(R.id.report_loading).setVisibility(View.GONE);
                                if (rules.getSubredditRules().size() > 0) {
                                    TextView subHeader = new TextView(mContext);
                                    subHeader.setText(mContext.getString(R.string.report_sub_rules,
                                            submission.getSubredditName()));
                                    reasonGroup.addView(subHeader, reasonGroup.getChildCount() - 2);
                                }
                                for (SubredditRule rule : rules.getSubredditRules()) {
                                    if (rule.getKind() == SubredditRule.RuleKind.LINK
                                            || rule.getKind() == SubredditRule.RuleKind.ALL) {
                                        RadioButton btn = new RadioButton(mContext);
                                        btn.setText(rule.getViolationReason());
                                        reasonGroup.addView(btn, reasonGroup.getChildCount() - 2);
                                        btn.getLayoutParams().width = WindowManager.LayoutParams.MATCH_PARENT;
                                    }
                                }
                                if (rules.getSiteRules().size() > 0) {
                                    TextView siteHeader = new TextView(mContext);
                                    siteHeader.setText(R.string.report_site_rules);
                                    reasonGroup.addView(siteHeader, reasonGroup.getChildCount() - 2);
                                }
                                for (String rule : rules.getSiteRules()) {
                                    RadioButton btn = new RadioButton(mContext);
                                    btn.setText(rule);
                                    reasonGroup.addView(btn, reasonGroup.getChildCount() - 2);
                                    btn.getLayoutParams().width = WindowManager.LayoutParams.MATCH_PARENT;
                                }
                            }
                        }.execute();

                        reportDialog.show();
                        break;
                    case 8:
                        if(SettingValues.shareLongLink){
                            Reddit.defaultShareText(submission.getTitle(), "https://reddit.com" + submission.getPermalink(), mContext);
                        } else {
                            Reddit.defaultShareText(submission.getTitle(), "https://redd.it/" + submission.getId(), mContext);
                        }
                        break;
                    case 6: {
                        ClipboardManager clipboard = ContextCompat.getSystemService(mContext,
                                ClipboardManager.class);
                        ClipData clip = ClipData.newPlainText("Link", submission.getUrl());
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                        }
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
                                                ClipboardManager clipboard =
                                                        ContextCompat.getSystemService(mContext,
                                                                ClipboardManager.class);
                                                ClipData clip;
                                                if (!selected.isEmpty()) {
                                                    clip = ClipData.newPlainText("Selftext",
                                                            selected);

                                                } else {
                                                    clip = ClipData.newPlainText("Selftext",
                                                            HtmlCompat.fromHtml(
                                                                    submission.getTitle()
                                                                            + "\n\n"
                                                                            + submission.getSelftext(), HtmlCompat.FROM_HTML_MODE_LEGACY));

                                                }
                                                if (clipboard != null) {
                                                    clipboard.setPrimaryClip(clip);
                                                }
                                                Toast.makeText(mContext,
                                                        R.string.submission_comment_copied,
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                .setNegativeButton(R.string.btn_cancel, null)
                                .setNeutralButton("COPY ALL",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ClipboardManager clipboard =
                                                        ContextCompat.getSystemService(mContext,
                                                                ClipboardManager.class);
                                                ClipData clip = ClipData.newPlainText("Selftext",
                                                        StringEscapeUtils.unescapeHtml4(
                                                                submission.getTitle()
                                                                        + "\n\n"
                                                                        + submission.getSelftext()));
                                                if (clipboard != null) {
                                                    clipboard.setPrimaryClip(clip);
                                                }

                                                Toast.makeText(mContext,
                                                        R.string.submission_text_copied,
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

    private void saveSubmission(final Submission submission, final Activity mContext,
            final SubmissionViewHolder holder, final boolean full) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (ActionStates.isSaved(submission)) {
                        new AccountManager(Authentication.reddit).unsave(submission);
                        ActionStates.setSaved(submission, false);
                    } else {
                        new AccountManager(Authentication.reddit).save(submission);
                        ActionStates.setSaved(submission, true);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Snackbar s;
                try {
                    if (ActionStates.isSaved(submission)) {

                        ((ImageView) holder.save).setColorFilter(
                                ContextCompat.getColor(mContext, R.color.md_amber_500),
                                PorterDuff.Mode.SRC_ATOP);
                        holder.save.setContentDescription(mContext.getString(R.string.btn_unsave));
                        s = Snackbar.make(holder.itemView, R.string.submission_info_saved,
                                Snackbar.LENGTH_LONG);
                        if (Authentication.me.hasGold()) {
                            s.setAction(R.string.category_categorize, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    categorizeSaved(submission, holder.itemView, mContext);
                                }
                            });
                        }

                        AnimateHelper.setFlashAnimation(holder.itemView, holder.save,
                                ContextCompat.getColor(mContext, R.color.md_amber_500));
                    } else {
                        s = Snackbar.make(holder.itemView, R.string.submission_info_unsaved,
                                Snackbar.LENGTH_SHORT);
                        ((ImageView) holder.save).setColorFilter(
                                ((((holder.itemView.getTag(holder.itemView.getId())) != null
                                        && holder.itemView.getTag(holder.itemView.getId())
                                        .equals("none"))) || full) ? getCurrentTintColor(mContext)
                                        : getWhiteTintColor(), PorterDuff.Mode.SRC_ATOP);
                        holder.save.setContentDescription(mContext.getString(R.string.btn_save));

                    }
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } catch (Exception ignored) {

                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void categorizeSaved(final Submission submission, View itemView,
            final Context mContext) {
        new AsyncTask<Void, Void, List<String>>() {

            Dialog d;

            @Override
            public void onPreExecute() {
                d = new MaterialDialog.Builder(mContext).progress(true, 100)
                        .title(R.string.profile_category_loading)
                        .content(R.string.misc_please_wait)
                        .show();
            }

            @Override
            protected List<String> doInBackground(Void... params) {
                try {
                    List<String> categories = new ArrayList<String>(
                            new AccountManager(Authentication.reddit).getSavedCategories());
                    categories.add("New category");
                    return categories;
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<String>() {{
                        add("New category");
                    }};
                    //sub probably has no flairs?
                }
            }

            @Override
            public void onPostExecute(final List<String> data) {
                try {
                    new MaterialDialog.Builder(mContext).items(data)
                            .title(R.string.sidebar_select_flair)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, final View itemView,
                                        int which, CharSequence text) {
                                    final String t = data.get(which);
                                    if (which == data.size() - 1) {
                                        new MaterialDialog.Builder(mContext).title(
                                                R.string.category_set_name)
                                                .input(mContext.getString(
                                                        R.string.category_set_name_hint), null,
                                                        false, new MaterialDialog.InputCallback() {
                                                            @Override
                                                            public void onInput(
                                                                    MaterialDialog dialog,
                                                                    CharSequence input) {

                                                            }
                                                        })
                                                .positiveText(R.string.btn_set)
                                                .onPositive(
                                                        new MaterialDialog.SingleButtonCallback() {
                                                            @Override
                                                            public void onClick(
                                                                    MaterialDialog dialog,
                                                                    DialogAction which) {
                                                                final String flair =
                                                                        dialog.getInputEditText()
                                                                                .getText()
                                                                                .toString();
                                                                new AsyncTask<Void, Void, Boolean>() {
                                                                    @Override
                                                                    protected Boolean doInBackground(
                                                                            Void... params) {
                                                                        try {
                                                                            new AccountManager(
                                                                                    Authentication.reddit)
                                                                                    .save(submission,
                                                                                            flair);
                                                                            return true;
                                                                        } catch (ApiException e) {
                                                                            e.printStackTrace();
                                                                            return false;
                                                                        }
                                                                    }

                                                                    @Override
                                                                    protected void onPostExecute(
                                                                            Boolean done) {
                                                                        Snackbar s;
                                                                        if (done) {
                                                                            if (itemView != null) {
                                                                                s = Snackbar.make(
                                                                                        itemView,
                                                                                        R.string.submission_info_saved,
                                                                                        Snackbar.LENGTH_SHORT);
                                                                                View view =
                                                                                        s.getView();
                                                                                TextView tv =
                                                                                        view.findViewById(
                                                                                                com.google.android.material.R.id.snackbar_text);
                                                                                tv.setTextColor(
                                                                                        Color.WHITE);
                                                                                s.show();
                                                                            }
                                                                        } else {
                                                                            if (itemView != null) {
                                                                                s = Snackbar.make(
                                                                                        itemView,
                                                                                        R.string.category_set_error,
                                                                                        Snackbar.LENGTH_SHORT);
                                                                                View view =
                                                                                        s.getView();
                                                                                TextView tv =
                                                                                        view.findViewById(
                                                                                                com.google.android.material.R.id.snackbar_text);
                                                                                tv.setTextColor(
                                                                                        Color.WHITE);
                                                                                s.show();
                                                                            }
                                                                        }

                                                                    }
                                                                }.executeOnExecutor(
                                                                        AsyncTask.THREAD_POOL_EXECUTOR);
                                                            }
                                                        })
                                                .negativeText(R.string.btn_cancel)
                                                .show();
                                    } else {
                                        new AsyncTask<Void, Void, Boolean>() {
                                            @Override
                                            protected Boolean doInBackground(Void... params) {
                                                try {
                                                    new AccountManager(Authentication.reddit).save(
                                                            submission, t);
                                                    return true;
                                                } catch (ApiException e) {
                                                    e.printStackTrace();
                                                    return false;
                                                }
                                            }

                                            @Override
                                            protected void onPostExecute(Boolean done) {
                                                Snackbar s;
                                                if (done) {
                                                    if (itemView != null) {
                                                        s = Snackbar.make(itemView,
                                                                R.string.submission_info_saved,
                                                                Snackbar.LENGTH_SHORT);
                                                        View view = s.getView();
                                                        TextView tv = view.findViewById(
                                                                com.google.android.material.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        s.show();
                                                    }
                                                } else {
                                                    if (itemView != null) {
                                                        s = Snackbar.make(itemView,
                                                                R.string.category_set_error,
                                                                Snackbar.LENGTH_SHORT);
                                                        View view = s.getView();
                                                        TextView tv = view.findViewById(
                                                                com.google.android.material.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        s.show();
                                                    }
                                                }
                                            }
                                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    }
                                }
                            })
                            .show();
                    if (d != null) {
                        d.dismiss();
                    }
                } catch (Exception ignored) {

                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
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
                TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snack.show();
            }

        }
    }

    public <T extends Contribution> void showModBottomSheet(final Activity mContext,
            final Submission submission, final List<T> posts, final SubmissionViewHolder holder,
            final RecyclerView recyclerview, final Map<String, Integer> reports,
            final Map<String, String> reports2) {

        final Resources res = mContext.getResources();
        int[] attrs = new int[]{R.attr.tintColor};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.profile, null);
        final Drawable report =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.report, null);
        final Drawable approve =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.support, null);
        final Drawable nsfw =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.hide, null);
        final Drawable spoiler =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.spoil, null);
        final Drawable pin =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sub, null);
        final Drawable lock =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.lock, null);
        final Drawable flair = ResourcesCompat.getDrawable(mContext.getResources(),
                R.drawable.quote, null);
        final Drawable remove =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.close, null);
        final Drawable remove_reason =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.report_reason, null);
        final Drawable ban =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ban, null);
        final Drawable spam =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.spam, null);
        final Drawable distinguish =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.star,
                        null);
        final Drawable note = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.note, null);


        profile.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        report.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        approve.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        spam.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        nsfw.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        pin.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        flair.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        remove.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        spoiler.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        remove_reason.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        ban.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        spam.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        distinguish.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        lock.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        note.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));

        ta.recycle();

        BottomSheet.Builder b =
                new BottomSheet.Builder(mContext).title(HtmlCompat.fromHtml(submission.getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));

        int reportCount = reports.size() + reports2.size();

        b.sheet(0, report,
                res.getQuantityString(R.plurals.mod_btn_reports, reportCount, reportCount));

        if (SettingValues.toolboxEnabled) {
            b.sheet(24, note, res.getString(R.string.mod_usernotes_view));
        }

        boolean approved = false;
        String whoApproved = "";
        b.sheet(1, approve, res.getString(R.string.mod_btn_approve));
        b.sheet(6, remove, mContext.getString(R.string.mod_btn_remove))
                .sheet(7, remove_reason, res.getString(R.string.mod_btn_remove_reason))
                .sheet(30, spam, res.getString(R.string.mod_btn_spam));

        // b.sheet(2, spam, mContext.getString(R.string.mod_btn_spam)) todo this
        b.sheet(20, flair, res.getString(R.string.mod_btn_submission_flair));

        final boolean isNsfw = submission.isNsfw();
        if (isNsfw) {
            b.sheet(3, nsfw, res.getString(R.string.mod_btn_unmark_nsfw));
        } else {
            b.sheet(3, nsfw, res.getString(R.string.mod_btn_mark_nsfw));
        }

        final boolean isSpoiler = submission.getDataNode().get("spoiler").asBoolean();
        if (isSpoiler) {
            b.sheet(12, nsfw, res.getString(R.string.mod_btn_unmark_spoiler));
        } else {
            b.sheet(12, nsfw, res.getString(R.string.mod_btn_mark_spoiler));
        }

        final boolean locked = submission.isLocked();
        if (locked) {
            b.sheet(9, lock, res.getString(R.string.mod_btn_unlock_thread));
        } else {
            b.sheet(9, lock, res.getString(R.string.mod_btn_lock_thread));
        }

        final boolean stickied = submission.isStickied();
        if (!SubmissionCache.removed.contains(submission.getFullName())) {
            if (stickied) {
                b.sheet(4, pin, res.getString(R.string.mod_btn_unpin));
            } else {
                b.sheet(4, pin, res.getString(R.string.mod_btn_pin));
            }
        }

        final boolean distinguished =
                submission.getDistinguishedStatus() == DistinguishedStatus.MODERATOR
                        || submission.getDistinguishedStatus() == DistinguishedStatus.ADMIN;
        if (submission.getAuthor().equalsIgnoreCase(Authentication.name)) {
            if (distinguished) {
                b.sheet(5, distinguish, "Undistingiush");
            } else {
                b.sheet(5, distinguish, "Distinguish");
            }
        }

        final String finalWhoApproved = whoApproved;
        final boolean finalApproved = approved;
        b.sheet(8, profile, res.getString(R.string.mod_btn_author));
        b.sheet(23, ban, mContext.getString(R.string.mod_ban_user));
        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        new AsyncTask<Void, Void, ArrayList<String>>() {
                            @Override
                            protected ArrayList<String> doInBackground(Void... params) {

                                ArrayList<String> finalReports = new ArrayList<>();
                                for (Map.Entry<String, Integer> entry : reports.entrySet()) {
                                    finalReports.add(entry.getValue() + "× " + entry.getKey());
                                }
                                for (Map.Entry<String, String> entry : reports2.entrySet()) {
                                    finalReports.add(entry.getKey() + ": " + entry.getValue());
                                }
                                if (finalReports.isEmpty()) {
                                    finalReports.add(mContext.getString(R.string.mod_no_reports));
                                }
                                return finalReports;
                            }

                            @Override
                            public void onPostExecute(ArrayList<String> data) {
                                new AlertDialogWrapper.Builder(mContext).setTitle(
                                        R.string.mod_reports)
                                        .setItems(data.toArray(new CharSequence[0]),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                            int which) {

                                                    }
                                                })
                                        .show();
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        break;
                    case 1:
                        if (finalApproved) {
                            Intent i = new Intent(mContext, Profile.class);
                            i.putExtra(Profile.EXTRA_PROFILE, finalWhoApproved);
                            mContext.startActivity(i);
                        } else {
                            approveSubmission(mContext, posts, submission, recyclerview, holder);
                        }
                        break;
                    case 2:
                        //todo this
                        break;
                    case 3:
                        if (isNsfw) {
                            unNsfwSubmission(mContext, submission, holder);
                        } else {
                            setPostNsfw(mContext, submission, holder);
                        }
                        break;
                    case 12:
                        if (isSpoiler) {
                            unSpoiler(mContext, submission, holder);
                        } else {
                            setSpoiler(mContext, submission, holder);
                        }
                        break;
                    case 9:
                        if (locked) {
                            unLockSubmission(mContext, submission, holder);
                        } else {
                            lockSubmission(mContext, submission, holder);
                        }
                        break;
                    case 4:
                        if (stickied) {
                            unStickySubmission(mContext, submission, holder);
                        } else {
                            stickySubmission(mContext, submission, holder);
                        }
                        break;
                    case 5:
                        if (distinguished) {
                            unDistinguishSubmission(mContext, submission, holder);
                        } else {
                            distinguishSubmission(mContext, submission, holder);
                        }
                        break;
                    case 6:
                        removeSubmission(mContext, submission, posts, recyclerview, holder, false);
                        break;
                    case 7:
                        if (SettingValues.removalReasonType == SettingValues.RemovalReasonType.TOOLBOX.ordinal()
                                && ToolboxUI.canShowRemoval(submission.getSubredditName())) {
                            ToolboxUI.showRemoval(mContext, submission, new ToolboxUI.CompletedRemovalCallback() {
                                @Override
                                public void onComplete(boolean success) {
                                    if (success) {
                                        SubmissionCache.removed.add(submission.getFullName());
                                        SubmissionCache.approved.remove(submission.getFullName());

                                        SubmissionCache.updateInfoSpannable(submission, mContext,
                                                submission.getSubredditName());

                                        if (mContext instanceof ModQueue) {
                                            final int pos = posts.indexOf(submission);
                                            posts.remove(submission);

                                            if (pos == 0) {
                                                recyclerview.getAdapter().notifyDataSetChanged();
                                            } else {
                                                recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                                            }
                                        } else {
                                            recyclerview.getAdapter().notifyItemChanged(holder.getAdapterPosition());
                                        }
                                        Snackbar s = Snackbar.make(holder.itemView, R.string.submission_removed,
                                                Snackbar.LENGTH_LONG);

                                        View view = s.getView();
                                        TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                                        tv.setTextColor(Color.WHITE);
                                        s.show();

                                    } else {
                                        new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                                                .setMessage(R.string.err_retry_later)
                                                .show();
                                    }
                                }
                            });
                        } else { // Show a Slide reason dialog if we can't show a toolbox or reddit one
                            doRemoveSubmissionReason(mContext, submission, posts, recyclerview, holder);
                        }
                        break;
                    case 30:
                        removeSubmission(mContext, submission, posts, recyclerview, holder, true);
                        break;
                    case 8:
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                        mContext.startActivity(i);
                        break;
                    case 20:
                        doSetFlair(mContext, submission, holder);
                        break;
                    case 23:
                        //ban a user
                        showBan(mContext, holder.itemView, submission, "", "", "", "");
                        break;
                    case 24:
                        ToolboxUI.showUsernotes(mContext, submission.getAuthor(), submission.getSubredditName(),
                                "l," + submission.getId());
                        break;
                }
            }
        });


        b.show();
    }

    private <T extends Contribution> void doRemoveSubmissionReason(final Activity mContext,
            final Submission submission, final List<T> posts, final RecyclerView recyclerview,
            final SubmissionViewHolder holder) {
        reason = "";
        new MaterialDialog.Builder(mContext).title(R.string.mod_remove_title)
                .positiveText(R.string.btn_remove)
                .alwaysCallInputCallback()
                .input(mContext.getString(R.string.mod_remove_hint),
                        mContext.getString(R.string.mod_remove_template), false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                reason = input.toString();
                            }
                        })
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .neutralText(R.string.mod_remove_insert_draft)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {

                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(final MaterialDialog dialog, DialogAction which) {

                        removeSubmissionReason(submission, mContext, posts, reason, holder,
                                recyclerview);

                    }
                })
                .negativeText(R.string.btn_cancel)
                .onNegative(null)
                .show();
    }

    private <T extends Contribution> void removeSubmissionReason(final Submission submission,
            final Activity mContext, final List<T> posts, final String reason,
            final SubmissionViewHolder holder, final RecyclerView recyclerview) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    SubmissionCache.removed.add(submission.getFullName());
                    SubmissionCache.approved.remove(submission.getFullName());

                    SubmissionCache.updateInfoSpannable(submission, mContext,
                            submission.getSubredditName());

                    if (mContext instanceof ModQueue) {
                        final int pos = posts.indexOf(submission);
                        posts.remove(submission);

                        if (pos == 0) {
                            recyclerview.getAdapter().notifyDataSetChanged();
                        } else {
                            recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                        }
                    } else {
                        recyclerview.getAdapter().notifyItemChanged(holder.getAdapterPosition());
                    }
                    Snackbar s = Snackbar.make(holder.itemView, R.string.submission_removed,
                            Snackbar.LENGTH_LONG);

                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    String toDistinguish = new AccountManager(Authentication.reddit).reply(submission, reason);
                    new ModerationManager(Authentication.reddit).remove(submission, false);
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(
                            Authentication.reddit.get("t1_" + toDistinguish).get(0),
                            DistinguishedStatus.MODERATOR);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private <T extends Contribution> void removeSubmission(final Activity mContext,
            final Submission submission, final List<T> posts, final RecyclerView recyclerview,
            final SubmissionViewHolder holder, final boolean spam) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {

                SubmissionCache.removed.add(submission.getFullName());
                SubmissionCache.approved.remove(submission.getFullName());

                SubmissionCache.updateInfoSpannable(submission, mContext,
                        submission.getSubredditName());

                if (b) {
                    if (mContext instanceof ModQueue) {
                        final int pos = posts.indexOf(submission);
                        posts.remove(submission);

                        if (pos == 0) {
                            recyclerview.getAdapter().notifyDataSetChanged();
                        } else {
                            recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                        }
                    } else {
                        recyclerview.getAdapter().notifyItemChanged(holder.getAdapterPosition());
                    }


                    Snackbar s = Snackbar.make(holder.itemView, R.string.submission_removed,
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).remove(submission, spam);
                } catch (ApiException | NetworkException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void doSetFlair(final Activity mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, ArrayList<String>>() {
            ArrayList<FlairTemplate> flair;

            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                FlairReference allFlairs = new FluentRedditClient(Authentication.reddit).subreddit(
                        submission.getSubredditName()).flair();
                try {
                    flair = new ArrayList<>(allFlairs.options(submission));
                    final ArrayList<String> finalFlairs = new ArrayList<>();
                    for (FlairTemplate temp : flair) {
                        finalFlairs.add(temp.getText());
                    }
                    return finalFlairs;
                } catch (Exception e) {
                    e.printStackTrace();
                    //sub probably has no flairs?
                }
                return null;
            }

            @Override
            public void onPostExecute(final ArrayList<String> data) {
                try {
                    if (data.isEmpty()) {
                        new AlertDialogWrapper.Builder(mContext).setTitle(
                                R.string.mod_flair_none_found)
                                .setPositiveButton(R.string.btn_ok, null)
                                .show();
                    } else {
                        showFlairSelectionDialog(mContext, submission, data, flair, holder);
                    }
                } catch (Exception ignored) {

                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showFlairSelectionDialog(final Activity mContext, final Submission submission,
            ArrayList<String> data, final ArrayList<FlairTemplate> flair,
            final SubmissionViewHolder holder) {
        new MaterialDialog.Builder(mContext).items(data)
                .title(R.string.sidebar_select_flair)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which,
                            CharSequence text) {
                        final FlairTemplate t = flair.get(which);
                        if (t.isTextEditable()) {
                            showFlairEditDialog(mContext, submission, t, holder);
                        } else {
                            setFlair(mContext, null, submission, t, holder);
                        }
                    }
                })
                .show();
    }

    private void showFlairEditDialog(final Activity mContext, final Submission submission,
            final FlairTemplate t, final SubmissionViewHolder holder) {
        new MaterialDialog.Builder(mContext).title(R.string.sidebar_select_flair_text)
                .input(mContext.getString(R.string.mod_flair_hint), t.getText(), true,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                            }
                        })
                .positiveText(R.string.btn_set)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        final String flair = dialog.getInputEditText().getText().toString();
                        setFlair(mContext, flair, submission, t, holder);
                    }
                })
                .negativeText(R.string.btn_cancel)
                .show();
    }

    private void setFlair(final Context mContext, final String flair, final Submission submission,
            final FlairTemplate t, final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setFlair(
                            submission.getSubredditName(), t, flair, submission);
                    return true;
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean done) {
                Snackbar s = null;
                if (done) {
                    if (holder.itemView != null) {
                        s = Snackbar.make(holder.itemView, R.string.snackbar_flair_success,
                                Snackbar.LENGTH_SHORT);
                    }
                    if (holder.itemView != null) {
                        SubmissionCache.updateTitleFlair(submission, flair, mContext);
                        doText(holder, submission, mContext, submission.getSubredditName(), false);
                    }
                } else {
                    if (holder.itemView != null) {
                        s = Snackbar.make(holder.itemView, R.string.snackbar_flair_error,
                                Snackbar.LENGTH_SHORT);
                    }
                }
                if (s != null) {
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public void doText(SubmissionViewHolder holder, Submission submission, Context mContext,
            String baseSub, boolean full) {
        SpannableStringBuilder t = SubmissionCache.getTitleLine(submission, mContext);
        SpannableStringBuilder l = SubmissionCache.getInfoLine(submission, mContext, baseSub);
        SpannableStringBuilder c = SubmissionCache.getCrosspostLine(submission, mContext);

        int[] textSizeAttr = new int[]{R.attr.font_cardtitle, R.attr.font_cardinfo};
        TypedArray a = mContext.obtainStyledAttributes(textSizeAttr);
        int textSizeT = a.getDimensionPixelSize(0, 18);
        int textSizeI = a.getDimensionPixelSize(1, 14);


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
        if(!full && c != null){
            c.setSpan(new AbsoluteSizeSpan(textSizeI), 0, c.length(), 0);
            s.append("\n");
            s.append(c);
        }
        a.recycle();

        holder.title.setText(s);

    }

    private void stickySubmission(final Activity mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s =
                            Snackbar.make(holder.itemView, R.string.really_pin_submission_message,
                                    Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setSticky(submission, true);
                } catch (ApiException | NetworkException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void unStickySubmission(final Activity mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s =
                            Snackbar.make(holder.itemView, R.string.really_unpin_submission_message,
                                    Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setSticky(submission, false);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void lockSubmission(final Activity mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s =
                            Snackbar.make(holder.itemView, R.string.mod_locked, Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setLocked(submission);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void unLockSubmission(final Activity mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s =
                            Snackbar.make(holder.itemView, R.string.mod_unlocked, Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setUnlocked(submission);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void distinguishSubmission(final Activity mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, "Submission distinguished",
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(submission,
                            DistinguishedStatus.MODERATOR);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void unDistinguishSubmission(final Activity mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, "Submission distinguish removed",
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(submission,
                            DistinguishedStatus.MODERATOR);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setPostNsfw(final Activity mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s =
                            Snackbar.make(holder.itemView, "NSFW status set", Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }

            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setNsfw(submission, true);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void unNsfwSubmission(final Context mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        //todo update view with NSFW tag
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, "NSFW status removed",
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }

            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setNsfw(submission, false);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setSpoiler(final Activity mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, "Spoiler status set",
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }

            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setSpoiler(submission, true);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void unSpoiler(final Context mContext, final Submission submission,
            final SubmissionViewHolder holder) {
        //todo update view with NSFW tag
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, "Spoiler status removed",
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }

            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).setSpoiler(submission, false);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private <T extends Thing> void approveSubmission(final Context mContext, final List<T> posts,
            final Submission submission, final RecyclerView recyclerview,
            final SubmissionViewHolder holder) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    SubmissionCache.approved.add(submission.getFullName());
                    SubmissionCache.removed.remove(submission.getFullName());
                    SubmissionCache.updateInfoSpannable(submission, mContext,
                            submission.getSubredditName());

                    if (mContext instanceof ModQueue) {
                        final int pos = posts.indexOf(submission);
                        posts.remove(submission);

                        if (pos == 0) {
                            recyclerview.getAdapter().notifyDataSetChanged();
                        } else {
                            recyclerview.getAdapter().notifyItemRemoved(pos + 1);
                        }
                    } else {
                        recyclerview.getAdapter().notifyItemChanged(holder.getAdapterPosition());
                    }

                    try {
                        Snackbar s = Snackbar.make(holder.itemView, R.string.mod_approved,
                                Snackbar.LENGTH_LONG);
                        View view = s.getView();
                        TextView tv = view.findViewById(
                                com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        s.show();
                    } catch (Exception ignored) {

                    }

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).approve(submission);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void showBan(final Context mContext, final View mToolbar, final Submission submission,
            String rs, String nt, String msg, String t) {
        LinearLayout l = new LinearLayout(mContext);
        l.setOrientation(LinearLayout.VERTICAL);
        int sixteen = Reddit.dpToPxVertical(16);
        l.setPadding(sixteen, 0, sixteen, 0);

        final EditText reason = new EditText(mContext);
        reason.setHint(R.string.mod_ban_reason);
        reason.setText(rs);
        reason.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        l.addView(reason);


        final EditText note = new EditText(mContext);
        note.setHint(R.string.mod_ban_note_mod);
        note.setText(nt);
        note.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        l.addView(note);

        final EditText message = new EditText(mContext);
        message.setHint(R.string.mod_ban_note_user);
        message.setText(msg);
        message.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        l.addView(message);

        final EditText time = new EditText(mContext);
        time.setHint(R.string.mod_ban_time);
        time.setText(t);
        time.setInputType(InputType.TYPE_CLASS_NUMBER);
        l.addView(time);

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
        builder.setView(l)
                .setTitle(mContext.getString(R.string.mod_ban_title, submission.getAuthor()))
                .setCancelable(true)
                .setPositiveButton(R.string.mod_btn_ban, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //to ban
                                if (reason.getText().toString().isEmpty()) {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(
                                            R.string.mod_ban_reason_required)
                                            .setMessage(R.string.misc_please_try_again)
                                            .setPositiveButton(R.string.btn_ok,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                int which) {
                                                            showBan(mContext, mToolbar, submission,
                                                                    reason.getText().toString(),
                                                                    note.getText().toString(),
                                                                    message.getText().toString(),
                                                                    time.getText().toString());
                                                        }
                                                    })
                                            .setCancelable(false)
                                            .show();
                                } else {
                                    new AsyncTask<Void, Void, Boolean>() {
                                        @Override
                                        protected Boolean doInBackground(Void... params) {
                                            try {
                                                String n = note.getText().toString();
                                                String m = message.getText().toString();

                                                if (n.isEmpty()) {
                                                    n = null;
                                                }
                                                if (m.isEmpty()) {
                                                    m = null;
                                                }
                                                if (time.getText().toString().isEmpty()) {
                                                    new ModerationManager(
                                                            Authentication.reddit).banUserPermanently(
                                                            submission.getSubredditName(),
                                                            submission.getAuthor(),
                                                            reason.getText().toString(), n, m);
                                                } else {
                                                    new ModerationManager(Authentication.reddit).banUser(
                                                            submission.getSubredditName(),
                                                            submission.getAuthor(),
                                                            reason.getText().toString(), n, m,
                                                            Integer.parseInt(time.getText().toString()));
                                                }
                                                return true;
                                            } catch (Exception e) {
                                                if (e instanceof InvalidScopeException) {
                                                    scope = true;
                                                }
                                                e.printStackTrace();
                                                return false;
                                            }
                                        }

                                        boolean scope;

                                        @Override
                                        protected void onPostExecute(Boolean done) {
                                            Snackbar s;
                                            if (done) {
                                                s = Snackbar.make(mToolbar, R.string.mod_ban_success,
                                                        Snackbar.LENGTH_SHORT);
                                            } else {
                                                if (scope) {
                                                    new AlertDialogWrapper.Builder(mContext).setTitle(
                                                            R.string.mod_ban_reauth)
                                                            .setMessage(R.string.mod_ban_reauth_question)
                                                            .setPositiveButton(R.string.btn_ok,
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(
                                                                                DialogInterface dialog,
                                                                                int which) {
                                                                            Intent i = new Intent(mContext,
                                                                                    Reauthenticate.class);
                                                                            mContext.startActivity(i);
                                                                        }
                                                                    })
                                                            .setNegativeButton(R.string.misc_maybe_later,
                                                                    null)
                                                            .setCancelable(false)
                                                            .show();
                                                }
                                                s = Snackbar.make(mToolbar, R.string.mod_ban_fail,
                                                        Snackbar.LENGTH_INDEFINITE)
                                                        .setAction(R.string.misc_try_again,
                                                                new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        showBan(mContext, mToolbar,
                                                                                submission,
                                                                                reason.getText().toString(),
                                                                                note.getText().toString(),
                                                                                message.getText()
                                                                                        .toString(),
                                                                                time.getText().toString());
                                                                    }
                                                                });

                                            }

                                            if (s != null)

                                            {
                                                View view = s.getView();
                                                TextView tv = view.findViewById(
                                                        com.google.android.material.R.id.snackbar_text);
                                                tv.setTextColor(Color.WHITE);
                                                s.show();
                                            }
                                        }
                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                        }

                )
                .setNegativeButton(R.string.btn_cancel, null)
                .show();

    }


    public <T extends Contribution> void populateSubmissionViewHolder(
            final SubmissionViewHolder holder, final Submission submission, final Activity mContext,
            boolean fullscreen, final boolean full, final List<T> posts,
            final RecyclerView recyclerview, final boolean same, final boolean offline,
            final String baseSub, @Nullable final CommentAdapter adapter) {
        holder.itemView.findViewById(R.id.vote).setVisibility(View.GONE);


        if (!offline
                && UserSubscriptions.modOf != null
                && submission.getSubredditName() != null
                && UserSubscriptions.modOf.contains(
                submission.getSubredditName().toLowerCase(Locale.ENGLISH))) {
            holder.mod.setVisibility(View.VISIBLE);
            final Map<String, Integer> reports = submission.getUserReports();
            final Map<String, String> reports2 = submission.getModeratorReports();
            if (reports.size() + reports2.size() > 0) {
                ((ImageView) holder.mod).setColorFilter(
                        ContextCompat.getColor(mContext, R.color.md_red_300),
                        PorterDuff.Mode.SRC_ATOP);
            } else {
                ((ImageView) holder.mod).setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
            }
            holder.mod.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showModBottomSheet(mContext, submission, posts, holder, recyclerview, reports,
                            reports2);
                }
            });
        } else {
            holder.mod.setVisibility(View.GONE);
        }

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
        holder.comments.setText(String.format(Locale.getDefault(), "%d %s", commentCount,
                ((more > 0 && SettingValues.commentLastVisit) ? "(+" + more + ")" : "")));
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


        final ImageView downvotebutton = (ImageView) holder.downvote;
        final ImageView upvotebutton = (ImageView) holder.upvote;

        if (submission.isArchived()) {
            downvotebutton.setVisibility(View.GONE);
            upvotebutton.setVisibility(View.GONE);
        } else if (Authentication.isLoggedIn && Authentication.didOnline) {
            if (SettingValues.actionbarVisible && downvotebutton.getVisibility() != View.VISIBLE) {
                downvotebutton.setVisibility(View.VISIBLE);
                upvotebutton.setVisibility(View.VISIBLE);
            }
        }

        //Set the colors and styles for the score text depending on what state it is in
        //Also set content descriptions
        switch (ActionStates.getVoteDirection(submission)) {
            case UPVOTE: {
                holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                upvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500),
                        PorterDuff.Mode.SRC_ATOP);
                upvotebutton.setContentDescription(mContext.getString(R.string.btn_upvoted));
                holder.score.setTypeface(null, Typeface.BOLD);
                downvotebutton.setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
                downvotebutton.setContentDescription(mContext.getString(R.string.btn_downvote));
                if (submission.getVote() != VoteDirection.UPVOTE) {
                    if (submission.getVote() == VoteDirection.DOWNVOTE) ++submissionScore;
                    ++submissionScore; //offset the score by +1
                }
                break;
            }
            case DOWNVOTE: {
                holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
                downvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_blue_500),
                        PorterDuff.Mode.SRC_ATOP);
                downvotebutton.setContentDescription(mContext.getString(R.string.btn_downvoted));
                holder.score.setTypeface(null, Typeface.BOLD);
                upvotebutton.setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
                upvotebutton.setContentDescription(mContext.getString(R.string.btn_upvote));
                if (submission.getVote() != VoteDirection.DOWNVOTE) {
                    if (submission.getVote() == VoteDirection.UPVOTE) --submissionScore;
                    --submissionScore; //offset the score by +1
                }
                break;
            }
            case NO_VOTE: {
                holder.score.setTextColor(holder.comments.getCurrentTextColor());
                holder.score.setTypeface(null, Typeface.NORMAL);
                downvotebutton.setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
                upvotebutton.setContentDescription(mContext.getString(R.string.btn_upvote));
                upvotebutton.setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
                downvotebutton.setContentDescription(mContext.getString(R.string.btn_downvote));
                break;
            }
        }


        //if the submission is already at 0pts, keep it at 0pts
        submissionScore = Math.max(submissionScore, 0);
        if (submissionScore >= 10000 && SettingValues.abbreviateScores) {
            holder.score.setText(String.format(Locale.getDefault(), "%.1fk",
                    (((double) submissionScore) / 1000)));
        } else {
            holder.score.setText(String.format(Locale.getDefault(), "%d", submissionScore));
        }

        //Save the score so we can use it in the OnClickListeners for the vote buttons
        final int SUBMISSION_SCORE = submissionScore;

        final ImageView hideButton = (ImageView) holder.hide;
        if (hideButton != null) {
            if (SettingValues.hideButton && Authentication.isLoggedIn) {
                hideButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSubmission(submission, posts, baseSub, recyclerview, mContext);
                    }
                });
            } else {
                hideButton.setVisibility(View.GONE);
            }
        }
        if (Authentication.isLoggedIn && Authentication.didOnline) {
            if (ActionStates.isSaved(submission)) {
                ((ImageView) holder.save).setColorFilter(
                        ContextCompat.getColor(mContext, R.color.md_amber_500),
                        PorterDuff.Mode.SRC_ATOP);
                holder.save.setContentDescription(mContext.getString(R.string.btn_unsave));
            } else {
                ((ImageView) holder.save).setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
                holder.save.setContentDescription(mContext.getString(R.string.btn_save));
            }
            holder.save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSubmission(submission, mContext, holder, full);
                }
            });
        }

        if (!SettingValues.saveButton && !full
                || !Authentication.isLoggedIn
                || !Authentication.didOnline) {
            holder.save.setVisibility(View.GONE);
        }

        ImageView thumbImage2 = ((ImageView) holder.thumbimage);

        if (holder.leadImage.thumbImage2 == null) {
            holder.leadImage.setThumbnail(thumbImage2);
        }

        final ContentType.Type type = ContentType.getContentType(submission);

        addClickFunctions(holder.leadImage, type, mContext, submission, holder, full);

        if (thumbImage2 != null) {
            addClickFunctions(thumbImage2, type, mContext, submission, holder, full);
        }

        if (full) {
            addClickFunctions(holder.itemView.findViewById(R.id.wraparea), type, mContext,
                    submission, holder, full);
        }

        if (full) {
            holder.leadImage.setWrapArea(holder.itemView.findViewById(R.id.wraparea));
        }

        if (full && (submission.getDataNode() != null
                && submission.getDataNode()
                .has("crosspost_parent_list")
                && submission.getDataNode().get("crosspost_parent_list") != null
                && submission.getDataNode().get("crosspost_parent_list").get(0) != null)) {
            holder.itemView.findViewById(R.id.crosspost).setVisibility(View.VISIBLE);
            ((TextView)holder.itemView.findViewById(R.id.crossinfo)).setText(SubmissionCache.getCrosspostLine(submission, mContext));
            ((Reddit) mContext.getApplicationContext()).getImageLoader()
                    .displayImage(submission.getDataNode().get("crosspost_parent_list").get(0).get("thumbnail").asText(), ((ImageView)holder.itemView.findViewById(R.id.crossthumb)));
            holder.itemView.findViewById(R.id.crosspost).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OpenRedditLink.openUrl(mContext, submission.getDataNode().get("crosspost_parent_list").get(0).get("permalink").asText(), true);
                }
            });
        }


        holder.leadImage.setSubmission(submission, full, baseSub, type);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (offline) {
                    Snackbar s =
                            Snackbar.make(holder.itemView, mContext.getString(R.string.offline_msg),
                                    Snackbar.LENGTH_SHORT);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
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

        doText(holder, submission, mContext, baseSub, full);

        if (!full
                && SettingValues.isSelftextEnabled(baseSub)
                && submission.isSelfPost()
                && !submission.getSelftext().isEmpty()
                && !submission.isNsfw()
                && !submission.getDataNode().get("spoiler").asBoolean()
                && !submission.getDataNode().get("selftext_html").asText().trim().isEmpty()) {
            holder.body.setVisibility(View.VISIBLE);
            String text = submission.getDataNode().get("selftext_html").asText();
            int typef = new FontPreferences(mContext).getFontTypeComment().getTypeface();
            Typeface typeface;
            if (typef >= 0) {
                typeface = RobotoTypefaces.obtainTypeface(mContext, typef);
            } else {
                typeface = Typeface.DEFAULT;
            }
            holder.body.setTypeface(typeface);

            holder.body.setTextHtml(HtmlCompat.fromHtml(
                    text.substring(0, text.contains("\n") ? text.indexOf("\n") : text.length()), HtmlCompat.FROM_HTML_MODE_LEGACY)
                    .toString()
                    .replace("<sup>", "<sup><small>")
                    .replace("</sup>", "</small></sup>"), "none ");
            holder.body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.itemView.callOnClick();
                }
            });
            holder.body.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holder.menu.callOnClick();
                    return true;
                }
            });
        } else if (!full) {
            holder.body.setVisibility(View.GONE);
        }

        if (full) {
            if (!submission.getSelftext().isEmpty()) {
                int typef = new FontPreferences(mContext).getFontTypeComment().getTypeface();
                Typeface typeface;
                if (typef >= 0) {
                    typeface = RobotoTypefaces.obtainTypeface(mContext, typef);
                } else {
                    typeface = Typeface.DEFAULT;
                }
                holder.firstTextView.setTypeface(typeface);

                setViews(submission.getDataNode().get("selftext_html").asText(),
                        submission.getSubredditName() == null ? "all"
                                : submission.getSubredditName(), holder);
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.body_area).setVisibility(View.GONE);
            }
        }

        try {
            final TextView points = holder.score;
            final TextView comments = holder.comments;

            if (Authentication.isLoggedIn && !offline && Authentication.didOnline) {
                {
                    downvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (SettingValues.storeHistory && !full) {
                                if (!submission.isNsfw() || SettingValues.storeNSFWHistory) {
                                    HasSeen.addSeen(submission.getFullName());
                                    if (mContext instanceof MainActivity) {
                                        holder.title.setAlpha(0.54f);
                                        holder.body.setAlpha(0.54f);
                                    }
                                }
                            }
                            if (ActionStates.getVoteDirection(submission)
                                    != VoteDirection.DOWNVOTE) { //has not been downvoted
                                points.setTextColor(
                                        ContextCompat.getColor(mContext, R.color.md_blue_500));
                                downvotebutton.setColorFilter(
                                        ContextCompat.getColor(mContext, R.color.md_blue_500),
                                        PorterDuff.Mode.SRC_ATOP);
                                upvotebutton.setColorFilter(
                                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                                && holder.itemView.getTag(holder.itemView.getId())
                                                .equals("none") || full)) ? getCurrentTintColor(
                                                mContext) : getWhiteTintColor(),
                                        PorterDuff.Mode.SRC_ATOP);
                                downvotebutton.setContentDescription(mContext.getString(R.string.btn_downvoted));

                                AnimateHelper.setFlashAnimation(holder.itemView, downvotebutton,
                                        ContextCompat.getColor(mContext, R.color.md_blue_500));
                                holder.score.setTypeface(null, Typeface.BOLD);
                                final int DOWNVOTE_SCORE = (SUBMISSION_SCORE == 0) ? 0 :
                                        SUBMISSION_SCORE
                                                - 1; //if a post is at 0 votes, keep it at 0 when downvoting
                                new Vote(false, points, mContext).execute(submission);
                                ActionStates.setVoteDirection(submission, VoteDirection.DOWNVOTE);
                                setSubmissionScoreText(submission, holder);
                            } else { //un-downvoted a post
                                points.setTextColor(comments.getCurrentTextColor());
                                new Vote(points, mContext).execute(submission);
                                holder.score.setTypeface(null, Typeface.NORMAL);
                                ActionStates.setVoteDirection(submission, VoteDirection.NO_VOTE);
                                downvotebutton.setColorFilter(
                                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                                && holder.itemView.getTag(holder.itemView.getId())
                                                .equals("none") || full)) ? getCurrentTintColor(
                                                mContext) : getWhiteTintColor(),
                                        PorterDuff.Mode.SRC_ATOP);
                                downvotebutton.setContentDescription(mContext.getString(R.string.btn_downvote));
                                setSubmissionScoreText(submission, holder);
                            }
                            if (!full
                                    && !SettingValues.actionbarVisible
                                    && SettingValues.defaultCardView
                                    != CreateCardView.CardEnum.DESKTOP) {
                                CreateCardView.toggleActionbar(holder.itemView);
                            }
                        }
                    });
                }
                {
                    upvotebutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (SettingValues.storeHistory && !full) {
                                if (!submission.isNsfw() || SettingValues.storeNSFWHistory) {
                                    HasSeen.addSeen(submission.getFullName());
                                    if (mContext instanceof MainActivity) {
                                        holder.title.setAlpha(0.54f);
                                        holder.body.setAlpha(0.54f);
                                    }
                                }
                            }

                            if (ActionStates.getVoteDirection(submission)
                                    != VoteDirection.UPVOTE) { //has not been upvoted
                                points.setTextColor(
                                        ContextCompat.getColor(mContext, R.color.md_orange_500));
                                upvotebutton.setColorFilter(
                                        ContextCompat.getColor(mContext, R.color.md_orange_500),
                                        PorterDuff.Mode.SRC_ATOP);
                                downvotebutton.setColorFilter(
                                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                                && holder.itemView.getTag(holder.itemView.getId())
                                                .equals("none") || full)) ? getCurrentTintColor(
                                                mContext) : getWhiteTintColor(),
                                        PorterDuff.Mode.SRC_ATOP);
                                upvotebutton.setContentDescription(mContext.getString(R.string.btn_upvoted));

                                AnimateHelper.setFlashAnimation(holder.itemView, upvotebutton,
                                        ContextCompat.getColor(mContext, R.color.md_orange_500));
                                holder.score.setTypeface(null, Typeface.BOLD);


                                new Vote(true, points, mContext).execute(submission);
                                ActionStates.setVoteDirection(submission, VoteDirection.UPVOTE);
                                setSubmissionScoreText(submission, holder);

                            } else { //un-upvoted a post
                                points.setTextColor(comments.getCurrentTextColor());
                                new Vote(points, mContext).execute(submission);
                                holder.score.setTypeface(null, Typeface.NORMAL);
                                ActionStates.setVoteDirection(submission, VoteDirection.NO_VOTE);
                                upvotebutton.setColorFilter(
                                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                                && holder.itemView.getTag(holder.itemView.getId())
                                                .equals("none") || full)) ? getCurrentTintColor(
                                                mContext) : getWhiteTintColor(),
                                        PorterDuff.Mode.SRC_ATOP);
                                upvotebutton.setContentDescription(mContext.getString(R.string.btn_upvote));
                                setSubmissionScoreText(submission, holder);

                            }
                            if (!full
                                    && !SettingValues.actionbarVisible
                                    && SettingValues.defaultCardView
                                    != CreateCardView.CardEnum.DESKTOP) {
                                CreateCardView.toggleActionbar(holder.itemView);
                            }
                        }
                    });
                }
            } else {
                upvotebutton.setVisibility(View.GONE);
                downvotebutton.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        final View edit = holder.edit;

        if (Authentication.name != null
                && Authentication.name.toLowerCase(Locale.ENGLISH)
                .equals(submission.getAuthor().toLowerCase(Locale.ENGLISH))
                && Authentication.didOnline) {
            edit.setVisibility(View.VISIBLE);
            edit.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    new AsyncTask<Void, Void, ArrayList<String>>() {
                        List<FlairTemplate> flairlist;

                        @Override
                        protected ArrayList<String> doInBackground(Void... params) {
                            FlairReference allFlairs =
                                    new FluentRedditClient(Authentication.reddit).subreddit(
                                            submission.getSubredditName()).flair();
                            try {
                                flairlist = allFlairs.options(submission);
                                final ArrayList<String> finalFlairs = new ArrayList<>();
                                for (FlairTemplate temp : flairlist) {
                                    finalFlairs.add(temp.getText());
                                }
                                return finalFlairs;
                            } catch (Exception e) {
                                e.printStackTrace();
                                //sub probably has no flairs?
                            }


                            return null;
                        }

                        @Override
                        public void onPostExecute(final ArrayList<String> data) {
                            final boolean flair = (data != null && !data.isEmpty());


                            int[] attrs = new int[]{R.attr.tintColor};
                            TypedArray ta = mContext.obtainStyledAttributes(attrs);

                            final int color2 = ta.getColor(0, Color.WHITE);
                            Drawable edit_drawable =
                                    mContext.getResources().getDrawable(R.drawable.edit);
                            Drawable nsfw_drawable =
                                    mContext.getResources().getDrawable(R.drawable.hide);
                            Drawable delete_drawable =
                                    mContext.getResources().getDrawable(R.drawable.delete);
                            Drawable flair_drawable =
                                    mContext.getResources().getDrawable(R.drawable.fontsize);

                            edit_drawable.setColorFilter(new PorterDuffColorFilter(color2, PorterDuff.Mode.SRC_ATOP));
                            nsfw_drawable.setColorFilter(new PorterDuffColorFilter(color2, PorterDuff.Mode.SRC_ATOP));
                            delete_drawable.setColorFilter(new PorterDuffColorFilter(color2, PorterDuff.Mode.SRC_ATOP));
                            flair_drawable.setColorFilter(new PorterDuffColorFilter(color2, PorterDuff.Mode.SRC_ATOP));

                            ta.recycle();

                            BottomSheet.Builder b = new BottomSheet.Builder(mContext).title(
                                            HtmlCompat.fromHtml(submission.getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));

                            if (submission.isSelfPost()) {
                                b.sheet(1, edit_drawable,
                                        mContext.getString(R.string.edit_selftext));
                            }
                            if (submission.isNsfw()) {
                                b.sheet(4, nsfw_drawable,
                                        mContext.getString(R.string.mod_btn_unmark_nsfw));
                            } else {
                                b.sheet(4, nsfw_drawable,
                                        mContext.getString(R.string.mod_btn_mark_nsfw));
                            }
                            if (submission.getDataNode().get("spoiler").asBoolean()) {
                                b.sheet(5, nsfw_drawable, mContext.getString(R.string.mod_btn_unmark_spoiler));
                            } else {
                                b.sheet(5, nsfw_drawable, mContext.getString(R.string.mod_btn_mark_spoiler));
                            }

                            b.sheet(2, delete_drawable,
                                    mContext.getString(R.string.delete_submission));

                            if (flair) {
                                b.sheet(3, flair_drawable,
                                        mContext.getString(R.string.set_submission_flair));

                            }

                            b.listener(new DialogInterface.OnClickListener()

                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 1: {
                                            LayoutInflater inflater = mContext.getLayoutInflater();

                                            final View dialoglayout =
                                                    inflater.inflate(R.layout.edit_comment, null);
                                            final AlertDialogWrapper.Builder builder =
                                                    new AlertDialogWrapper.Builder(mContext);

                                            final EditText e = dialoglayout.findViewById(
                                                    R.id.entry);
                                            e.setText(StringEscapeUtils.unescapeHtml4(
                                                    submission.getSelftext()));

                                            DoEditorActions.doActions(e, dialoglayout,
                                                    ((AppCompatActivity) mContext).getSupportFragmentManager(),
                                                    mContext, null, null);

                                            builder.setCancelable(false).setView(dialoglayout);
                                            final Dialog d = builder.create();
                                            d.getWindow()
                                                    .setSoftInputMode(
                                                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                                            d.show();
                                            dialoglayout.findViewById(R.id.cancel)
                                                    .setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            d.dismiss();
                                                        }
                                                    });
                                            dialoglayout.findViewById(R.id.submit)
                                                    .setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            final String text =
                                                                    e.getText().toString();
                                                            new AsyncTask<Void, Void, Void>() {
                                                                @Override
                                                                protected Void doInBackground(
                                                                        Void... params) {
                                                                    try {
                                                                        new AccountManager(
                                                                                Authentication.reddit)
                                                                                .updateContribution(
                                                                                        submission,
                                                                                        text);
                                                                        if (adapter != null) {
                                                                            adapter.dataSet.reloadSubmission(
                                                                                    adapter);
                                                                        }
                                                                        d.dismiss();
                                                                    } catch (Exception e) {
                                                                        (mContext).runOnUiThread(
                                                                                new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        new AlertDialogWrapper.Builder(
                                                                                                mContext)
                                                                                                .setTitle(
                                                                                                        R.string.comment_delete_err)
                                                                                                .setMessage(
                                                                                                        R.string.comment_delete_err_msg)
                                                                                                .setPositiveButton(
                                                                                                        R.string.btn_yes,
                                                                                                        new DialogInterface.OnClickListener() {
                                                                                                            @Override
                                                                                                            public void onClick(
                                                                                                                    DialogInterface dialog,
                                                                                                                    int which) {
                                                                                                                dialog.dismiss();
                                                                                                                doInBackground();
                                                                                                            }
                                                                                                        })
                                                                                                .setNegativeButton(
                                                                                                        R.string.btn_no,
                                                                                                        new DialogInterface.OnClickListener() {
                                                                                                            @Override
                                                                                                            public void onClick(
                                                                                                                    DialogInterface dialog,
                                                                                                                    int which) {
                                                                                                                dialog.dismiss();
                                                                                                            }
                                                                                                        })
                                                                                                .show();
                                                                                    }
                                                                                });
                                                                    }
                                                                    return null;
                                                                }

                                                                @Override
                                                                protected void onPostExecute(
                                                                        Void aVoid) {
                                                                    if (adapter != null) {
                                                                        adapter.notifyItemChanged(
                                                                                1);
                                                                    }
                                                                }
                                                            }.executeOnExecutor(
                                                                    AsyncTask.THREAD_POOL_EXECUTOR);
                                                        }
                                                    });
                                        }
                                        break;
                                        case 2: {
                                            new AlertDialogWrapper.Builder(mContext).setTitle(
                                                    R.string.really_delete_submission)
                                                    .setPositiveButton(R.string.btn_yes,
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(
                                                                        DialogInterface dialog,
                                                                        int which) {
                                                                    new AsyncTask<Void, Void, Void>() {
                                                                        @Override
                                                                        protected Void doInBackground(
                                                                                Void... params) {
                                                                            try {
                                                                                new ModerationManager(
                                                                                        Authentication.reddit)
                                                                                        .delete(submission);
                                                                            } catch (ApiException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                            return null;
                                                                        }

                                                                        @Override
                                                                        protected void onPostExecute(
                                                                                Void aVoid) {
                                                                            (mContext).runOnUiThread(
                                                                                    new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            (holder.title)
                                                                                                    .setTextHtml(
                                                                                                            mContext.getString(
                                                                                                                    R.string.content_deleted));
                                                                                            if (holder.firstTextView
                                                                                                    != null) {
                                                                                                holder.firstTextView
                                                                                                        .setText(
                                                                                                                R.string.content_deleted);
                                                                                                holder.commentOverflow
                                                                                                        .setVisibility(
                                                                                                                View.GONE);
                                                                                            } else {
                                                                                                if (holder.itemView
                                                                                                        .findViewById(
                                                                                                                R.id.body)
                                                                                                        != null) {
                                                                                                    ((TextView) holder.itemView
                                                                                                            .findViewById(
                                                                                                                    R.id.body))
                                                                                                            .setText(
                                                                                                                    R.string.content_deleted);
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }.executeOnExecutor(
                                                                            AsyncTask.THREAD_POOL_EXECUTOR);
                                                                }
                                                            })
                                                    .setNegativeButton(R.string.btn_cancel, null)
                                                    .show();
                                        }
                                        break;
                                        case 3: {
                                            new MaterialDialog.Builder(mContext).items(data)
                                                    .title(R.string.sidebar_select_flair)
                                                    .itemsCallback(
                                                            new MaterialDialog.ListCallback() {
                                                                @Override
                                                                public void onSelection(
                                                                        MaterialDialog dialog,
                                                                        View itemView, int which,
                                                                        CharSequence text) {
                                                                    final FlairTemplate t =
                                                                            flairlist.get(which);
                                                                    if (t.isTextEditable()) {
                                                                        new MaterialDialog.Builder(
                                                                                mContext).title(
                                                                                R.string.mod_btn_submission_flair_text)
                                                                                .input(mContext.getString(
                                                                                        R.string.mod_flair_hint),
                                                                                        t.getText(),
                                                                                        true,
                                                                                        new MaterialDialog.InputCallback() {
                                                                                            @Override
                                                                                            public void onInput(
                                                                                                    MaterialDialog dialog,
                                                                                                    CharSequence input) {

                                                                                            }
                                                                                        })
                                                                                .positiveText(
                                                                                        R.string.btn_set)
                                                                                .onPositive(
                                                                                        new MaterialDialog.SingleButtonCallback() {
                                                                                            @Override
                                                                                            public void onClick(
                                                                                                    MaterialDialog dialog,
                                                                                                    DialogAction which) {
                                                                                                final String
                                                                                                        flair =
                                                                                                        dialog.getInputEditText()
                                                                                                                .getText()
                                                                                                                .toString();
                                                                                                new AsyncTask<Void, Void, Boolean>() {
                                                                                                    @Override
                                                                                                    protected Boolean doInBackground(
                                                                                                            Void... params) {
                                                                                                        try {
                                                                                                            new ModerationManager(
                                                                                                                    Authentication.reddit)
                                                                                                                    .setFlair(
                                                                                                                            submission
                                                                                                                                    .getSubredditName(),
                                                                                                                            t,
                                                                                                                            flair,
                                                                                                                            submission);
                                                                                                            return true;
                                                                                                        } catch (ApiException e) {
                                                                                                            e.printStackTrace();
                                                                                                            return false;
                                                                                                        }
                                                                                                    }

                                                                                                    @Override
                                                                                                    protected void onPostExecute(
                                                                                                            Boolean done) {
                                                                                                        Snackbar
                                                                                                                s =
                                                                                                                null;
                                                                                                        if (done) {
                                                                                                            if (holder.itemView
                                                                                                                    != null) {
                                                                                                                s =
                                                                                                                        Snackbar.make(
                                                                                                                                holder.itemView,
                                                                                                                                R.string.snackbar_flair_success,
                                                                                                                                Snackbar.LENGTH_SHORT);
                                                                                                                SubmissionCache
                                                                                                                        .updateTitleFlair(
                                                                                                                                submission,
                                                                                                                                flair,
                                                                                                                                mContext);
                                                                                                                holder.title
                                                                                                                        .setText(
                                                                                                                                SubmissionCache
                                                                                                                                        .getTitleLine(
                                                                                                                                                submission,
                                                                                                                                                mContext));
                                                                                                            }
                                                                                                        } else {
                                                                                                            if (holder.itemView
                                                                                                                    != null) {
                                                                                                                s =
                                                                                                                        Snackbar.make(
                                                                                                                                holder.itemView,
                                                                                                                                R.string.snackbar_flair_error,
                                                                                                                                Snackbar.LENGTH_SHORT);
                                                                                                            }
                                                                                                        }
                                                                                                        if (s
                                                                                                                != null) {
                                                                                                            View
                                                                                                                    view =
                                                                                                                    s.getView();
                                                                                                            TextView
                                                                                                                    tv =
                                                                                                                    view
                                                                                                                            .findViewById(
                                                                                                                                    com.google.android.material.R.id.snackbar_text);
                                                                                                            tv.setTextColor(
                                                                                                                    Color.WHITE);
                                                                                                            s.show();
                                                                                                        }
                                                                                                    }
                                                                                                }.executeOnExecutor(
                                                                                                        AsyncTask.THREAD_POOL_EXECUTOR);
                                                                                            }
                                                                                        })
                                                                                .negativeText(
                                                                                        R.string.btn_cancel)
                                                                                .show();
                                                                    } else {
                                                                        new AsyncTask<Void, Void, Boolean>() {
                                                                            @Override
                                                                            protected Boolean doInBackground(
                                                                                    Void... params) {
                                                                                try {
                                                                                    new ModerationManager(
                                                                                            Authentication.reddit)
                                                                                            .setFlair(
                                                                                                    submission
                                                                                                            .getSubredditName(),
                                                                                                    t,
                                                                                                    null,
                                                                                                    submission);
                                                                                    return true;
                                                                                } catch (ApiException e) {
                                                                                    e.printStackTrace();
                                                                                    return false;
                                                                                }
                                                                            }

                                                                            @Override
                                                                            protected void onPostExecute(
                                                                                    Boolean done) {
                                                                                Snackbar s = null;
                                                                                if (done) {
                                                                                    if (holder.itemView
                                                                                            != null) {
                                                                                        s =
                                                                                                Snackbar.make(
                                                                                                        holder.itemView,
                                                                                                        R.string.snackbar_flair_success,
                                                                                                        Snackbar.LENGTH_SHORT);
                                                                                        SubmissionCache
                                                                                                .updateTitleFlair(
                                                                                                        submission,
                                                                                                        t.getCssClass(),
                                                                                                        mContext);
                                                                                        holder.title
                                                                                                .setText(
                                                                                                        SubmissionCache
                                                                                                                .getTitleLine(
                                                                                                                        submission,
                                                                                                                        mContext));
                                                                                    }
                                                                                } else {
                                                                                    if (holder.itemView
                                                                                            != null) {
                                                                                        s =
                                                                                                Snackbar.make(
                                                                                                        holder.itemView,
                                                                                                        R.string.snackbar_flair_error,
                                                                                                        Snackbar.LENGTH_SHORT);
                                                                                    }
                                                                                }
                                                                                if (s != null) {
                                                                                    View view =
                                                                                            s.getView();
                                                                                    TextView tv =
                                                                                            view
                                                                                                    .findViewById(
                                                                                                            com.google.android.material.R.id.snackbar_text);
                                                                                    tv.setTextColor(
                                                                                            Color.WHITE);
                                                                                    s.show();
                                                                                }
                                                                            }
                                                                        }.executeOnExecutor(
                                                                                AsyncTask.THREAD_POOL_EXECUTOR);
                                                                    }
                                                                }
                                                            })
                                                    .show();
                                        }
                                        break;
                                        case 4:
                                            if (submission.isNsfw()) {
                                                unNsfwSubmission(mContext, submission, holder);
                                            } else {
                                                setPostNsfw(mContext, submission, holder);
                                            }
                                            break;
                                        case 5:
                                            if (submission.getDataNode().get("spoiler").asBoolean()) {
                                                unSpoiler(mContext, submission, holder);
                                            } else {
                                                setSpoiler(mContext, submission, holder);
                                            }
                                            break;
                                    }
                                }
                            }).show();
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } else {
            edit.setVisibility(View.GONE);
        }

        if (HasSeen.getSeen(submission) && !full) {
            holder.title.setAlpha(0.54f);
            holder.body.setAlpha(0.54f);
        } else {
            holder.title.setAlpha(1f);
            if (!full) {
                holder.body.setAlpha(1f);
            }
        }
    }

    private void setSubmissionScoreText(Submission submission, SubmissionViewHolder holder) {
        int submissionScore = submission.getScore();
        switch (ActionStates.getVoteDirection(submission)) {
            case UPVOTE: {
                if (submission.getVote() != VoteDirection.UPVOTE) {
                    if (submission.getVote() == VoteDirection.DOWNVOTE) ++submissionScore;
                    ++submissionScore; //offset the score by +1
                }
                break;
            }
            case DOWNVOTE: {
                if (submission.getVote() != VoteDirection.DOWNVOTE) {
                    if (submission.getVote() == VoteDirection.UPVOTE) --submissionScore;
                    --submissionScore; //offset the score by +1
                }
                break;
            }
            case NO_VOTE:
                if (submission.getVote() == VoteDirection.UPVOTE && submission.getAuthor()
                        .equalsIgnoreCase(Authentication.name)) {
                    submissionScore--;
                }
                break;
        }


        //if the submission is already at 0pts, keep it at 0pts
        submissionScore = Math.max(submissionScore, 0);
        if (submissionScore >= 10000 && SettingValues.abbreviateScores) {
            holder.score.setText(String.format(Locale.getDefault(), "%.1fk",
                    (((double) submissionScore) / 1000)));
        } else {
            holder.score.setText(String.format(Locale.getDefault(), "%d", submissionScore));
        }
    }

    private void setViews(String rawHTML, String subredditName, SubmissionViewHolder holder) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        if (!blocks.get(0).startsWith("<table>") && !blocks.get(0).startsWith("<pre>")) {
            holder.firstTextView.setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                holder.commentOverflow.setViews(blocks, subredditName);
            } else {
                holder.commentOverflow.setViews(blocks.subList(startIndex, blocks.size()),
                        subredditName);
            }
        }
    }

    public static class AsyncReportTask extends AsyncTask<String, Void, Void> {
        private Submission submission;
        private View contextView;

        public AsyncReportTask(Submission submission, View contextView) {
            this.submission = submission;
            this.contextView = contextView;
        }

        @Override
        protected Void doInBackground(String... reason) {
            try {
                new AccountManager(
                        Authentication.reddit).report(submission, reason[0]);
            } catch (ApiException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (contextView != null) {
                try {
                    Snackbar s = Snackbar.make(contextView, R.string.msg_report_sent, Snackbar.LENGTH_SHORT);
                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } catch (Exception ignored) {

                }
            }
        }
    }
}
