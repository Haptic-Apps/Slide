package me.ccrama.redditslide.SubmissionViews;


import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.robototextview.RobotoTypefaces;

import net.dean.jraw.ApiException;
import net.dean.jraw.fluent.FlairReference;
import net.dean.jraw.fluent.FluentRedditClient;
import net.dean.jraw.http.oauth.InvalidScopeException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thing;
import net.dean.jraw.models.VoteDirection;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.Album;
import me.ccrama.redditslide.Activities.AlbumPager;
import me.ccrama.redditslide.Activities.FullscreenVideo;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MediaView;
import me.ccrama.redditslide.Activities.ModQueue;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.PostReadLater;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Reauthenticate;
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
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.AnimateHelper;
import me.ccrama.redditslide.Views.BottomSheetHelper;
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
                                case VID_ME:
                                case STREAMABLE:
                                    if (SettingValues.video) {
                                        Intent myIntent =
                                                new Intent(contextActivity, MediaView.class);
                                        myIntent.putExtra(MediaView.SUBREDDIT,
                                                submission.getSubredditName());
                                        myIntent.putExtra(MediaView.EXTRA_URL, submission.getUrl());
                                        addAdaptorPosition(myIntent, submission,
                                                holder.getAdapterPosition());
                                        contextActivity.startActivity(myIntent);
                                    } else {
                                        LinkUtil.openExternally(submission.getUrl(),
                                                contextActivity);
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
                                        LinkUtil.openExternally(submission.getUrl(),
                                                contextActivity);
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
                                        LinkUtil.openExternally(submission.getUrl(),
                                                contextActivity);

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
                                        LinkUtil.openExternally(submission.getUrl(),
                                                contextActivity);

                                    }
                                    break;
                                case DEVIANTART:
                                case XKCD:
                                case IMAGE:
                                    openImage(type, contextActivity, submission, holder.leadImage,
                                            holder.getAdapterPosition());
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
                                    if (Reddit.videoPlugin) {
                                        try {
                                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                            sharingIntent.setClassName(
                                                    "ccrama.me.slideyoutubeplugin",
                                                    "ccrama.me.slideyoutubeplugin.YouTubeView");
                                            sharingIntent.putExtra("url", submission.getUrl());
                                            contextActivity.startActivity(sharingIntent);

                                        } catch (Exception e) {
                                            LinkUtil.openExternally(submission.getUrl(),
                                                    contextActivity);
                                        }
                                    } else {
                                        LinkUtil.openExternally(submission.getUrl(),
                                                contextActivity);
                                    }
                                    break;
                            }
                        } else {
                            LinkUtil.openExternally(submission.getUrl(), contextActivity);
                        }
                    }
                } else {
                    if (!(contextActivity instanceof PeekViewActivity)
                            || !((PeekViewActivity) contextActivity).isPeeking()) {

                        Snackbar s = Snackbar.make(holder.itemView, R.string.go_online_view_content,
                                Snackbar.LENGTH_SHORT);
                        View view = s.getView();
                        TextView tv = view.findViewById(
                                android.support.design.R.id.snackbar_text);
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
            LinkUtil.openExternally(submission.getUrl(), contextActivity);
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

            if (t == GifUtils.AsyncLoadGif.VideoType.VREDDIT) {
                if (submission.getDataNode().has("media") && submission.getDataNode()
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
                            .get("fallback_url")
                            .asText()).replace("&amp;", "&"));
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
            LinkUtil.openExternally(submission.getUrl(), contextActivity);
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
            final Submission submission, final SubmissionViewHolder holder, final List<T> posts,
            final String baseSub, final RecyclerView recyclerview, final boolean full) {

        final BottomSheetHelper bottomSheetHelper = new BottomSheetHelper(mContext);
        bottomSheetHelper.header(Html.fromHtml(submission.getTitle()),
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(
                                Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Link", submission.getUrl());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, R.string.submission_link_copied,
                                Toast.LENGTH_SHORT).show();
                        bottomSheetHelper.dismiss();
                        return true;
                    }
                });
        final boolean isReadLater = mContext instanceof PostReadLater;
        final boolean isAddedToReadLaterList = ReadLater.isToBeReadLater(submission);
        if (Authentication.didOnline) {
            bottomSheetHelper.textView("/u/" + submission.getAuthor(), R.drawable.profile,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, Profile.class);
                            i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                            mContext.startActivity(i);
                            bottomSheetHelper.dismiss();
                        }
                    }, new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            ClipboardManager clipboard =
                                    (ClipboardManager) mContext.getSystemService(
                                            Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Author name",
                                    "/u/" + submission.getAuthor());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(mContext, R.string.submission_author_copied,
                                    Toast.LENGTH_SHORT).show();
                            bottomSheetHelper.dismiss();
                            return true;
                        }
                    });
            bottomSheetHelper.textView("/r/" + submission.getSubredditName(), R.drawable.sub,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, SubredditView.class);
                            i.putExtra(SubredditView.EXTRA_SUBREDDIT,
                                    submission.getSubredditName());
                            mContext.startActivityForResult(i, 14);
                            bottomSheetHelper.dismiss();
                        }
                    }, new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            ClipboardManager clipboard =
                                    (ClipboardManager) mContext.getSystemService(
                                            Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Subreddit name",
                                    "/r/" + submission.getSubredditName());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(mContext, R.string.submission_subreddit_copied,
                                    Toast.LENGTH_SHORT).show();
                            bottomSheetHelper.dismiss();
                            return true;
                        }
                    });
            if (Authentication.isLoggedIn) {
                bottomSheetHelper.textView(
                        ActionStates.isSaved(submission) ? R.string.comment_unsave
                                : R.string.btn_save, R.drawable.iconstarfilled,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveSubmission(submission, mContext, holder, full);
                                bottomSheetHelper.dismiss();
                            }
                        });
            }
        }
        bottomSheetHelper.textView(isAddedToReadLaterList ? "Mark As Read" : "Read later",
                R.drawable.save, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                        bottomSheetHelper.dismiss();
                    }
                });
        if (Authentication.didOnline && Authentication.isLoggedIn) {
            bottomSheetHelper.textView(R.string.btn_report, R.drawable.report,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            reportReason = "";
                            new MaterialDialog.Builder(mContext).input(
                                    mContext.getString(R.string.input_reason_for_report), null,
                                    true, new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(MaterialDialog dialog,
                                                CharSequence input) {
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
                                        public void onClick(MaterialDialog dialog,
                                                DialogAction which) {
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
                                                            Snackbar s =
                                                                    Snackbar.make(holder.itemView,
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
                            bottomSheetHelper.dismiss();
                        }
                    });
        }
        if (submission.getSelftext() != null && !submission.getSelftext().isEmpty() && full) {
            bottomSheetHelper.textView(R.string.submission_copy_text, R.drawable.ic_content_copy,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
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
                                                                                        + submission
                                                                                        .getSelftext()));
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
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
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
                                            })
                                    .show();
                            bottomSheetHelper.dismiss();
                        }
                    });
        } else {
            bottomSheetHelper.textView(R.string.submission_link_copy, R.drawable.ic_content_copy,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager clipboard =
                                    (ClipboardManager) mContext.getSystemService(
                                            Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Link", submission.getUrl());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(mContext, R.string.submission_link_copied,
                                    Toast.LENGTH_SHORT).show();
                            bottomSheetHelper.dismiss();
                        }
                    });
        }
        if (!full && Authentication.didOnline) {
            bottomSheetHelper.textView(
                    submission.isHidden() ? R.string.submission_unhide : R.string.submission_hide,
                    R.drawable.hide, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hideSubmission(submission, posts, baseSub, recyclerview, mContext);
                            bottomSheetHelper.dismiss();
                        }
                    });
        }
        bottomSheetHelper.textView(R.string.submission_link_extern, R.drawable.openexternal,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinkUtil.openExternally(submission.getUrl(), mContext);
                        if (submission.isNsfw() && !SettingValues.storeNSFWHistory) {
                            //Do nothing if the post is NSFW and storeNSFWHistory is not enabled
                        } else if (SettingValues.storeHistory) {
                            HasSeen.addSeen(submission.getFullName());
                        }
                        bottomSheetHelper.dismiss();
                    }
                });
        bottomSheetHelper.textView(R.string.submission_share_permalink, R.drawable.link,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Reddit.defaultShareText(Html.fromHtml(submission.getTitle()).toString(),
                                StringEscapeUtils.escapeHtml4(submission.getUrl()), mContext);
                        bottomSheetHelper.dismiss();
                    }
                });
        bottomSheetHelper.textView(R.string.submission_share_reddit_url, R.drawable.commentchange,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Reddit.defaultShareText(Html.fromHtml(submission.getTitle()).toString(),
                                "https://reddit.com" + submission.getPermalink(), mContext);
                        bottomSheetHelper.dismiss();
                    }
                });
        if ((mContext instanceof MainActivity) || (mContext instanceof SubredditView)) {
            bottomSheetHelper.textView(R.string.filter_content, R.drawable.filter,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                                                        .toLowerCase(Locale.ENGLISH)),
                                        Arrays.asList(SettingValues.userFilters.toLowerCase(
                                                Locale.ENGLISH).split(",")).contains(
                                                submission.getAuthor().toLowerCase(Locale.ENGLISH)),
                                        Arrays.asList(SettingValues.domainFilters.toLowerCase(
                                                Locale.ENGLISH).split(",")).contains(
                                                submission.getDomain().toLowerCase(Locale.ENGLISH)),
                                        Arrays.asList(SettingValues.alwaysExternal.toLowerCase(
                                                Locale.ENGLISH).split(",")).contains(
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
                                        mContext.getString(R.string.filter_posts_flair, flair,
                                                baseSub)
                                };
                            }
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
                                            submission.getDomain().toLowerCase(Locale.ENGLISH)),
                                    Arrays.asList(
                                            SettingValues.flairFilters.toLowerCase(Locale.ENGLISH)
                                                    .split(",")).contains(baseSub + ":" + flair)
                            };
                            oldChosen = chosen.clone();

                            new AlertDialogWrapper.Builder(mContext).setTitle(R.string.filter_title)
                                    .alwaysCallMultiChoiceCallback()
                                    .setMultiChoiceItems(choices, chosen,
                                            new DialogInterface.OnMultiChoiceClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which, boolean isChecked) {
                                                    chosen[which] = isChecked;
                                                }
                                            })
                                    .setPositiveButton(R.string.filter_btn,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
                                                    boolean filtered = false;
                                                    SharedPreferences.Editor e =
                                                            SettingValues.prefs.edit();
                                                    if (chosen[0] && chosen[0] != oldChosen[0]) {
                                                        SettingValues.subredditFilters =
                                                                SettingValues.subredditFilters
                                                                        + (
                                                                        (SettingValues.subredditFilters
                                                                                .isEmpty()
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
                                                                SettingValues.userFilters
                                                                        + (
                                                                        (SettingValues.userFilters.isEmpty()
                                                                                || SettingValues.userFilters
                                                                                .endsWith(",")) ? ""
                                                                                : ",")
                                                                        + submission.getAuthor();
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
                                                                SettingValues.domainFilters
                                                                        + (
                                                                        (SettingValues.domainFilters
                                                                                .isEmpty()
                                                                                || SettingValues.domainFilters
                                                                                .endsWith(",")) ? ""
                                                                                : ",")
                                                                        + submission.getDomain();
                                                        filtered = true;
                                                        e.putString(
                                                                SettingValues.PREF_DOMAIN_FILTERS,
                                                                SettingValues.domainFilters);
                                                        PostMatch.domains = null;
                                                    } else if (!chosen[2]
                                                            && chosen[2] != oldChosen[2]) {
                                                        SettingValues.domainFilters =
                                                                SettingValues.domainFilters.replace(
                                                                        submission.getDomain(), "");
                                                        filtered = false;
                                                        e.putString(
                                                                SettingValues.PREF_DOMAIN_FILTERS,
                                                                SettingValues.domainFilters);
                                                        e.apply();
                                                        PostMatch.domains = null;
                                                    }
                                                    if (chosen[3] && chosen[3] != oldChosen[3]) {
                                                        SettingValues.alwaysExternal =
                                                                SettingValues.alwaysExternal
                                                                        + (
                                                                        (SettingValues.alwaysExternal
                                                                                .isEmpty()
                                                                                || SettingValues.alwaysExternal
                                                                                .endsWith(",")) ? ""
                                                                                : ",")
                                                                        + submission.getDomain();
                                                        e.putString(
                                                                SettingValues.PREF_ALWAYS_EXTERNAL,
                                                                SettingValues.alwaysExternal);
                                                        e.apply();
                                                    } else if (!chosen[3]
                                                            && chosen[3] != oldChosen[3]) {
                                                        SettingValues.alwaysExternal =
                                                                SettingValues.alwaysExternal.replace(
                                                                        submission.getDomain(), "");
                                                        e.putString(
                                                                SettingValues.PREF_ALWAYS_EXTERNAL,
                                                                SettingValues.alwaysExternal);
                                                        e.apply();
                                                    }
                                                    if (chosen.length > 4) {
                                                        if (chosen[4]
                                                                && chosen[4] != oldChosen[4]) {
                                                            SettingValues.flairFilters =
                                                                    SettingValues.flairFilters + (
                                                                            (SettingValues.flairFilters
                                                                                    .isEmpty()
                                                                                    || SettingValues.flairFilters
                                                                                    .endsWith(","))
                                                                                    ? "" : ",") + (
                                                                            baseSub
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
                                                            if (s instanceof Submission && PostMatch
                                                                    .doesMatch((Submission) s)) {
                                                                toRemove.add(s);
                                                            }
                                                        }

                                                        OfflineSubreddit s =
                                                                OfflineSubreddit.getSubreddit(
                                                                        baseSub, false, mContext);

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
                            bottomSheetHelper.dismiss();
                        }
                    });
        }
        bottomSheetHelper.build().show();
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
                    }
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                                                                                                android.support.design.R.id.snackbar_text);
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
                                                                                                android.support.design.R.id.snackbar_text);
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
                                                                android.support.design.R.id.snackbar_text);
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
                                                                android.support.design.R.id.snackbar_text);
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

    public <T extends Contribution> void showModBottomSheet(final Activity mContext,
            final Submission submission, final List<T> posts, final SubmissionViewHolder holder,
            final RecyclerView recyclerview, final Map<String, Integer> reports,
            final Map<String, String> reports2) {

        final BottomSheetHelper bottomSheetHelper = new BottomSheetHelper(mContext);
        bottomSheetHelper.header(Html.fromHtml(submission.getTitle()));
        int reportCount = reports.size() + reports2.size();
        bottomSheetHelper.textView(mContext.getResources()
                        .getQuantityString(R.plurals.mod_btn_reports, reportCount, reportCount),
                R.drawable.report, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                                        .setItems(data.toArray(new CharSequence[data.size()]),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                            int which) {

                                                    }
                                                })
                                        .show();
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        bottomSheetHelper.dismiss();
                    }
                });
        final String finalWhoApproved = "";
        final boolean finalApproved = false;
        bottomSheetHelper.textView(R.string.mod_btn_approve, R.drawable.support,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (finalApproved) {
                            Intent i = new Intent(mContext, Profile.class);
                            i.putExtra(Profile.EXTRA_PROFILE, finalWhoApproved);
                            mContext.startActivity(i);
                        } else {
                            approveSubmission(mContext, posts, submission, recyclerview, holder);
                        }
                        bottomSheetHelper.dismiss();
                    }
                });
        bottomSheetHelper.textView(R.string.mod_btn_remove, R.drawable.close,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeSubmission(mContext, submission, posts, recyclerview, holder, false);
                        bottomSheetHelper.dismiss();
                    }
                });
        bottomSheetHelper.textView(R.string.mod_btn_remove_reason, R.drawable.reportreason,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doRemoveSubmissionReason(mContext, submission, posts, recyclerview, holder);
                        bottomSheetHelper.dismiss();
                    }
                });
        bottomSheetHelper.textView("Mark as spam", R.drawable.spam, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSubmission(mContext, submission, posts, recyclerview, holder, true);
                bottomSheetHelper.dismiss();
            }
        });
        bottomSheetHelper.textView(R.string.mod_btn_submission_flair,
                R.drawable.ic_format_quote_white_48dp, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doSetFlair(mContext, submission, holder);
                        bottomSheetHelper.dismiss();
                    }
                });
        final boolean isNsfw = submission.isNsfw();
        bottomSheetHelper.textView(
                isNsfw ? R.string.mod_btn_unmark_nsfw : R.string.mod_btn_mark_nsfw, R.drawable.hide,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isNsfw) {
                            unNsfwSubmission(mContext, submission, holder);
                        } else {
                            setPostNsfw(mContext, submission, holder);
                        }
                        bottomSheetHelper.dismiss();
                    }
                });
        final boolean isSpoiler = submission.getDataNode().get("spoiler").asBoolean();
        bottomSheetHelper.textView(isSpoiler ? "Unmark as spoilter" : "Mark as spoiler",
                R.drawable.hide, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isSpoiler) {
                            unSpoiler(mContext, submission, holder);
                        } else {
                            setSpoiler(mContext, submission, holder);
                        }
                        bottomSheetHelper.dismiss();
                    }
                });
        final boolean isLocked = submission.isLocked();
        bottomSheetHelper.textView(isLocked ? "Unlock thread" : "Lock thread", R.drawable.lock,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isLocked) {
                            unLockSubmission(mContext, submission, holder);
                        } else {
                            lockSubmission(mContext, submission, holder);
                        }
                        bottomSheetHelper.dismiss();
                    }
                });
        final boolean isStickied = submission.isStickied();
        bottomSheetHelper.textView(isStickied ? R.string.mod_btn_unpin : R.string.mod_btn_pin,
                R.drawable.sub, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isStickied) {
                            unStickySubmission(mContext, submission, holder);
                        } else {
                            stickySubmission(mContext, submission, holder);
                        }
                        bottomSheetHelper.dismiss();
                    }
                });
        if (submission.getAuthor().equalsIgnoreCase(Authentication.name)) {
            final boolean isDistinguished =
                    submission.getDistinguishedStatus() == DistinguishedStatus.MODERATOR
                            || submission.getDistinguishedStatus() == DistinguishedStatus.ADMIN;
            bottomSheetHelper.textView(isDistinguished ? "Undistinguish" : "Distinguish",
                    R.drawable.iconstarfilled, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isDistinguished) {
                                unDistinguishSubmission(mContext, submission, holder);
                            } else {
                                distinguishSubmission(mContext, submission, holder);
                            }
                            bottomSheetHelper.dismiss();
                        }
                    });
        }
        bottomSheetHelper.textView(R.string.mod_btn_author, R.drawable.profile,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                        mContext.startActivity(i);
                        bottomSheetHelper.dismiss();
                    }
                });
        bottomSheetHelper.textView(R.string.mod_ban_user, R.drawable.ban,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showBan(mContext, holder.itemView, submission, "", "", "", "");
                        bottomSheetHelper.dismiss();
            }
        });
        bottomSheetHelper.build().show();
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                    String toGet =
                            new AccountManager(Authentication.reddit).reply(submission, reason);
                    new ModerationManager(Authentication.reddit).remove(submission, false);
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(
                            Authentication.reddit.get(toGet).get(0), DistinguishedStatus.MODERATOR);
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                } catch (ApiException e) {
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                } catch (ApiException e) {
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                            Snackbar.make(holder.itemView, "Thread locked", Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                            Snackbar.make(holder.itemView, "Thread unlocked", Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
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
                                android.support.design.R.id.snackbar_text);
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
                                                            Integer.valueOf(time.getText().toString()));
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
                                                        android.support.design.R.id.snackbar_text);
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
        switch (ActionStates.getVoteDirection(submission)) {
            case UPVOTE: {
                holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.md_orange_500));
                upvotebutton.setColorFilter(ContextCompat.getColor(mContext, R.color.md_orange_500),
                        PorterDuff.Mode.SRC_ATOP);
                holder.score.setTypeface(null, Typeface.BOLD);
                downvotebutton.setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
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
                holder.score.setTypeface(null, Typeface.BOLD);
                upvotebutton.setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
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
                upvotebutton.setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
                break;
            }
        }


        //if the submission is already at 0pts, keep it at 0pts
        submissionScore = ((submissionScore < 0) ? 0 : submissionScore);
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
            } else {
                ((ImageView) holder.save).setColorFilter(
                        (((holder.itemView.getTag(holder.itemView.getId())) != null
                                && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                || full)) ? getCurrentTintColor(mContext) : getWhiteTintColor(),
                        PorterDuff.Mode.SRC_ATOP);
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

        boolean crosspost = submission.getDataNode().has("crosspost_parent_list");
        if(full && crosspost){
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

            holder.body.setTextHtml(Html.fromHtml(
                    text.substring(0, text.contains("\n") ? text.indexOf("\n") : text.length()))
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

                            final BottomSheetHelper bottomSheetHelper =
                                    new BottomSheetHelper(mContext);
                            bottomSheetHelper.header(Html.fromHtml(submission.getTitle()));
                            if (submission.isSelfPost()) {
                                bottomSheetHelper.textView(R.string.edit_selftext, R.drawable.edit,
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                LayoutInflater inflater =
                                                        mContext.getLayoutInflater();

                                                final View dialoglayout =
                                                        inflater.inflate(R.layout.edit_comment,
                                                                null);
                                                final AlertDialogWrapper.Builder builder =
                                                        new AlertDialogWrapper.Builder(mContext);

                                                final EditText e =
                                                        dialoglayout.findViewById(R.id.entry);
                                                e.setText(StringEscapeUtils.unescapeHtml4(
                                                        submission.getSelftext()));

                                                DoEditorActions.doActions(e, dialoglayout,
                                                        ((AppCompatActivity) mContext).getSupportFragmentManager(),
                                                        mContext, null);

                                                builder.setCancelable(false).setView(dialoglayout);
                                                final Dialog d = builder.create();
                                                d.getWindow()
                                                        .setSoftInputMode(
                                                                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                                                d.show();
                                                dialoglayout.findViewById(R.id.cancel)
                                                        .setOnClickListener(
                                                                new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        d.dismiss();
                                                                    }
                                                                });
                                                dialoglayout.findViewById(R.id.submit)
                                                        .setOnClickListener(
                                                                new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        final String text =
                                                                                e.getText()
                                                                                        .toString();
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
                                                                                    if (adapter
                                                                                            != null) {
                                                                                        adapter.dataSet
                                                                                                .reloadSubmission(
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
                                                                                if (adapter
                                                                                        != null) {
                                                                                    adapter.notifyItemChanged(
                                                                                            1);
                                                                                }
                                                                            }
                                                                        }.executeOnExecutor(
                                                                                AsyncTask.THREAD_POOL_EXECUTOR);
                                                                    }
                                                                });
                                                bottomSheetHelper.dismiss();
                                            }
                                        });
                            }
                            bottomSheetHelper.textView(
                                    submission.isNsfw() ? R.string.mod_btn_unmark_nsfw
                                            : R.string.mod_btn_mark_nsfw, R.drawable.hide,
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (submission.isNsfw()) {
                                                unNsfwSubmission(mContext, submission, holder);
                                            } else {
                                                setPostNsfw(mContext, submission, holder);
                                            }
                                            bottomSheetHelper.dismiss();
                                        }
                                    });
                            bottomSheetHelper.textView(R.string.delete_submission,
                                    R.drawable.delete, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
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
                                            bottomSheetHelper.dismiss();
                                        }
                                    });
                            if (flair) {
                                bottomSheetHelper.textView(R.string.set_submission_flair,
                                        R.drawable.fontsizedarker, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                new MaterialDialog.Builder(mContext).items(data)
                                                        .title(R.string.sidebar_select_flair)
                                                        .itemsCallback(
                                                                new MaterialDialog.ListCallback() {
                                                                    @Override
                                                                    public void onSelection(
                                                                            MaterialDialog dialog,
                                                                            View itemView,
                                                                            int which,
                                                                            CharSequence text) {
                                                                        final FlairTemplate t =
                                                                                flairlist.get(
                                                                                        which);
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
                                                                                                                        view.findViewById(
                                                                                                                                android.support.design.R.id.snackbar_text);
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
                                                                                    Snackbar s =
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
                                                                                        TextView
                                                                                                tv =
                                                                                                view.findViewById(
                                                                                                        android.support.design.R.id.snackbar_text);
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
                                                bottomSheetHelper.dismiss();
                                            }
                                        });
                            }
                            bottomSheetHelper.build().show();
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
        submissionScore = ((submissionScore < 0) ? 0 : submissionScore);
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
}
