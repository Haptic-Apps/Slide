package me.ccrama.redditslide.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.text.HtmlCompat;

import com.devspark.robototextview.RobotoTypefaces;

import net.dean.jraw.models.Account;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.VoteDirection;

import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Adapters.ProfileCommentViewHolder;
import me.ccrama.redditslide.Adapters.SubmissionViewHolder;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ForceTouch.PeekViewActivity;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;


/**
 * Created by ccrama on 3/5/2015.
 */
public class RedditItemView extends RelativeLayout {

    OpenRedditLink.RedditLinkType contentType;


    public RedditItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RedditItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RedditItemView(Context context) {
        super(context);
        init();
    }


    public void loadUrl(PeekMediaView v, String url, ProgressBar progress) {

        this.progress = progress;
        Uri uri = OpenRedditLink.formatRedditUrl(url);

        if (uri == null) {
            v.doLoadLink(url);
        } else if (url.startsWith("np")) {
            uri = uri.buildUpon().authority(uri.getHost().substring(2)).build();
        }
        List<String> parts = uri.getPathSegments();

        contentType = OpenRedditLink.getRedditLinkType(uri);

        switch (contentType) {
            case SHORTENED: {
                new AsyncLoadSubmission(parts.get(0)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case LIVE: {
                v.doLoadLink(url);
            }
            break;
            case WIKI:
            case SEARCH:
            case OTHER: {
                v.doLoadLink(url);
                break;
            }
            case COMMENT_PERMALINK: {
                String submission = parts.get(3);
                if (parts.size() >= 6) {
                    //is likely a comment
                    String end = parts.get(5);

                    if (end.length() >= 3) {
                        new AsyncLoadComment(end).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new AsyncLoadSubmission(submission).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }

                } else {
                    new AsyncLoadSubmission(submission).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
            }
            case SUBMISSION: {
                new AsyncLoadSubmission(parts.get(3)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case SUBMISSION_WITHOUT_SUB: {
                new AsyncLoadSubmission(parts.get(1)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case SUBREDDIT: {
                new AsyncLoadSubreddit(parts.get(1)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }
            case USER: {
                String name = parts.get(1);
                new AsyncLoadProfile(name).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            }

        }
    }

    ProgressBar progress;

    public class AsyncLoadProfile extends AsyncTask<Void, Void, Account> {

        String id;

        public AsyncLoadProfile(String profileName) {
            this.id = profileName;
        }

        @Override
        protected Account doInBackground(Void... params) {
            try {
                return Authentication.reddit.getUser(id);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Account account) {
            if (account != null && (account.getDataNode().has("is_suspended")
                    && !account.getDataNode().get("is_suspended").asBoolean())) {
                View content = LayoutInflater.from(getContext())
                        .inflate(R.layout.account_pop, RedditItemView.this, false);
                RelativeLayout.LayoutParams params = (LayoutParams) content.getLayoutParams();
                params.addRule(CENTER_IN_PARENT);
                addView(content);
                doUser(account, content);
            }
            if (progress != null) {
                progress.setVisibility(GONE);
            }
        }
    }

    private void doUser(Account account, View content) {
        String name = account.getFullName();
        final TextView title = content.findViewById(R.id.title);
        title.setText(name);

        final int currentColor = Palette.getColorUser(name);
        title.setBackgroundColor(currentColor);

        String info = getContext().getString(R.string.profile_age,
                TimeUtils.getTimeSince(account.getCreated().getTime(), getContext()));

        ((TextView) content.findViewById(R.id.moreinfo)).setText(info);

        ((TextView) content.findViewById(R.id.commentkarma)).setText(
                String.format(Locale.getDefault(), "%d", account.getCommentKarma()));
        ((TextView) content.findViewById(R.id.linkkarma)).setText(
                String.format(Locale.getDefault(), "%d", account.getLinkKarma()));

    }

    public class AsyncLoadSubreddit extends AsyncTask<Void, Void, Subreddit> {

        String id;

        public AsyncLoadSubreddit(String subredditName) {
            this.id = subredditName;
        }

        @Override
        protected Subreddit doInBackground(Void... params) {
            try {
                return Authentication.reddit.getSubreddit(id);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Subreddit subreddit) {
            if (subreddit != null) {
                View content = LayoutInflater.from(getContext())
                        .inflate(R.layout.subreddit_pop, RedditItemView.this, false);
                RelativeLayout.LayoutParams params = (LayoutParams) content.getLayoutParams();
                params.addRule(CENTER_IN_PARENT);
                addView(content);
                doSidebar(subreddit, content);
            }
            if (progress != null) {
                progress.setVisibility(GONE);
            }
        }
    }

    private void doSidebar(Subreddit subreddit, View content) {
        if (Authentication.isLoggedIn ? subreddit.isUserSubscriber() : UserSubscriptions.getSubscriptions(getContext())
                .contains(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH))) {
            ((AppCompatCheckBox) content.findViewById(R.id.subscribed)).setChecked(true);
        }
        content.findViewById(R.id.header_sub)
                .setBackgroundColor(Palette.getColor(subreddit.getDisplayName()));
        ((TextView) content.findViewById(R.id.sub_infotitle)).setText(subreddit.getDisplayName());
        if (!subreddit.getPublicDescription().isEmpty()) {
            content.findViewById(R.id.sub_title).setVisibility(View.VISIBLE);
            setViews(subreddit.getDataNode().get("public_description_html").asText(),
                    subreddit.getDisplayName().toLowerCase(Locale.ENGLISH),
                    content.findViewById(R.id.sub_title),
                    content.findViewById(R.id.sub_title_overflow));
        } else {
            content.findViewById(R.id.sub_title).setVisibility(View.GONE);
        }
        if (subreddit.getDataNode().has("icon_img") && !subreddit.getDataNode()
                .get("icon_img")
                .asText()
                .isEmpty()) {
            ((Reddit) ((PeekViewActivity) getContext()).getApplication()).getImageLoader()
                    .displayImage(subreddit.getDataNode().get("icon_img").asText(),
                            (ImageView) content.findViewById(R.id.subimage));
        } else {
            content.findViewById(R.id.subimage).setVisibility(View.GONE);
        }
        if (findViewById(R.id.sub_banner) != null) {
            String bannerImage = subreddit.getBannerImage();
            if (bannerImage != null && !bannerImage.isEmpty()) {
                findViewById(R.id.sub_banner).setVisibility(View.VISIBLE);
                ((Reddit) ((PeekViewActivity) getContext()).getApplication()).getImageLoader()
                        .displayImage(bannerImage, (ImageView) findViewById(R.id.sub_banner));
            } else {
                findViewById(R.id.sub_banner).setVisibility(View.GONE);
            }
        }
        ((TextView) content.findViewById(R.id.subscribers)).setText(
                getContext().getString(R.string.subreddit_subscribers_string,
                        subreddit.getLocalizedSubscriberCount()));
        content.findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

        ((TextView) content.findViewById(R.id.active_users)).setText(
                getContext().getString(R.string.subreddit_active_users_string_new,
                        subreddit.getLocalizedAccountsActive()));
        content.findViewById(R.id.active_users).setVisibility(View.VISIBLE);
    }


    public class AsyncLoadComment extends AsyncTask<Void, Void, Comment> {

        String id;

        public AsyncLoadComment(String commentId) {
            this.id = commentId;
        }

        @Override
        protected Comment doInBackground(Void... params) {
            try {
                return (Comment) Authentication.reddit.get("t1_" + id).get(0);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Comment comment) {
            if (comment != null) {
                LogUtil.v("Adding view");
                View content = LayoutInflater.from(getContext())
                        .inflate(R.layout.profile_comment, RedditItemView.this, false);
                RelativeLayout.LayoutParams params = (LayoutParams) content.getLayoutParams();
                params.addRule(CENTER_IN_PARENT);
                addView(content);
                doComment(comment, content);
            }
            if (progress != null) {
                progress.setVisibility(GONE);
            }
        }
    }

    public class AsyncLoadSubmission extends AsyncTask<Void, Void, Submission> {

        String id;

        public AsyncLoadSubmission(String submissionId) {
            this.id = submissionId;
        }

        @Override
        protected Submission doInBackground(Void... params) {
            try {
                return Authentication.reddit.getSubmission(id);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Submission submission) {
            if (submission != null) {
                View content = CreateCardView.CreateView(RedditItemView.this);
                RelativeLayout.LayoutParams params = (LayoutParams) content.getLayoutParams();
                params.addRule(CENTER_IN_PARENT);
                addView(content);
                doSubmission(submission, content);
            }
            if (progress != null) {
                progress.setVisibility(GONE);
            }}
    }

    public void doComment(Comment comment, View content) {
        ProfileCommentViewHolder holder = new ProfileCommentViewHolder(content);
        String scoreText;
        if (comment.isScoreHidden()) {
            scoreText =
                    "[" + getContext().getString(R.string.misc_score_hidden).toUpperCase() + "]";
        } else {
            scoreText = String.format(Locale.getDefault(), "%d", comment.getScore());
        }

        SpannableStringBuilder score = new SpannableStringBuilder(scoreText);

        if (score == null || score.toString().isEmpty()) {
            score = new SpannableStringBuilder("0");
        }
        if (!scoreText.contains("[")) {
            score.append(String.format(Locale.getDefault(), " %s", getContext().getResources()
                    .getQuantityString(R.plurals.points, comment.getScore())));
        }
        holder.score.setText(score);

        if (Authentication.isLoggedIn) {
            if (ActionStates.getVoteDirection(comment) == VoteDirection.UPVOTE) {
                holder.score.setTextColor(
                        getContext().getResources().getColor(R.color.md_orange_500));
            } else if (ActionStates.getVoteDirection(comment) == VoteDirection.DOWNVOTE) {
                holder.score.setTextColor(
                        getContext().getResources().getColor(R.color.md_blue_500));
            } else {
                holder.score.setTextColor(holder.time.getCurrentTextColor());
            }
        }
        String spacer = getContext().getString(R.string.submission_properties_seperator);
        SpannableStringBuilder titleString = new SpannableStringBuilder();


        String timeAgo = TimeUtils.getTimeAgo(comment.getCreated().getTime(), getContext());
        String time = ((timeAgo == null || timeAgo.isEmpty()) ? "just now"
                : timeAgo); //some users were crashing here
        time = time + (((comment.getEditDate() != null) ? " (edit " + TimeUtils.getTimeAgo(
                comment.getEditDate().getTime(), getContext()) + ")" : ""));
        titleString.append(time);
        titleString.append(spacer);

        if (comment.getSubredditName() != null) {
            String subname = comment.getSubredditName();
            SpannableStringBuilder subreddit = new SpannableStringBuilder("/r/" + subname);
            if ((SettingValues.colorSubName
                    && Palette.getColor(subname) != Palette.getDefaultColor())) {
                subreddit.setSpan(new ForegroundColorSpan(Palette.getColor(subname)), 0,
                        subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                subreddit.setSpan(new StyleSpan(Typeface.BOLD), 0, subreddit.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            titleString.append(subreddit);
        }

        holder.time.setText(titleString);
        setViews(comment.getDataNode().get("body_html").asText(), comment.getSubredditName(),
                holder);

        int type = new FontPreferences(getContext()).getFontTypeComment().getTypeface();
        Typeface typeface;
        if (type >= 0) {
            typeface = RobotoTypefaces.obtainTypeface(getContext(), type);
        } else {
            typeface = Typeface.DEFAULT;
        }
        holder.content.setTypeface(typeface);

        if (!SettingValues.hideCommentAwards && (comment.getTimesSilvered() > 0 || comment.getTimesGilded() > 0  || comment.getTimesPlatinized() > 0)) {
            TypedArray a = getContext().obtainStyledAttributes(
                    new FontPreferences(getContext()).getPostFontStyle().getResId(),
                    R.styleable.FontStyle);
            int fontsize =
                    (int) (a.getDimensionPixelSize(R.styleable.FontStyle_font_cardtitle, -1) * .75);
            a.recycle();
            holder.gild.setVisibility(View.VISIBLE);
            // Add silver, gold, platinum icons and counts in that order
            if (comment.getTimesSilvered() > 0) {
                final String timesSilvered = (comment.getTimesSilvered() == 1) ? ""
                        : "\u200Ax" + comment.getTimesSilvered();
                SpannableStringBuilder silvered =
                        new SpannableStringBuilder("\u00A0★" + timesSilvered + "\u00A0");
                Bitmap image = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.silver);
                float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
                image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                        (int) Math.ceil(fontsize), true);
                silvered.setSpan(new ImageSpan(getContext(), image, ImageSpan.ALIGN_BASELINE), 0, 2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                silvered.setSpan(new RelativeSizeSpan(0.75f), 3, silvered.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ((TextView) holder.gild).append(silvered);
            }
            if (comment.getTimesGilded() > 0) {
                final String timesGilded = (comment.getTimesGilded() == 1) ? ""
                        : "\u200Ax" + comment.getTimesGilded();
                SpannableStringBuilder gilded =
                        new SpannableStringBuilder("\u00A0★" + timesGilded + "\u00A0");
                Bitmap image = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.gold);
                float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
                image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                        (int) Math.ceil(fontsize), true);
                gilded.setSpan(new ImageSpan(getContext(), image, ImageSpan.ALIGN_BASELINE), 0, 2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                gilded.setSpan(new RelativeSizeSpan(0.75f), 3, gilded.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ((TextView) holder.gild).append(gilded);
            }
            if (comment.getTimesPlatinized() > 0) {
                final String timesPlatinized = (comment.getTimesPlatinized() == 1) ? ""
                        : "\u200Ax" + comment.getTimesPlatinized();
                SpannableStringBuilder platinized =
                        new SpannableStringBuilder("\u00A0★" + timesPlatinized + "\u00A0");
                Bitmap image = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.platinum);
                float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
                image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                        (int) Math.ceil(fontsize), true);
                platinized.setSpan(new ImageSpan(getContext(), image, ImageSpan.ALIGN_BASELINE), 0, 2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                platinized.setSpan(new RelativeSizeSpan(0.75f), 3, platinized.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ((TextView) holder.gild).append(platinized);
            }
        } else if (holder.gild.getVisibility() == View.VISIBLE) {
            holder.gild.setVisibility(View.GONE);
        }

        if (comment.getSubmissionTitle() != null) {
            holder.title.setText(HtmlCompat.fromHtml(comment.getSubmissionTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else {
            holder.title.setText(HtmlCompat.fromHtml(comment.getAuthor(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    public void doSubmission(Submission submission, View content) {
        final SubmissionViewHolder holder = new SubmissionViewHolder(content);
        CreateCardView.resetColorCard(holder.itemView);
        if (submission.getSubredditName() != null) {
            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(Locale.ENGLISH), holder.itemView,
                    "no_subreddit", false);
        }
        new PopulateSubmissionViewHolder().populateSubmissionViewHolder(holder, submission,
                ((PeekViewActivity) getContext()), false, false, null, null, false, false, null,
                null);

    }

    private void init() {
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
            SidebarLayout sidebar = findViewById(R.id.drawer_layout);
            for (int i = 0; i < commentOverflow.getChildCount(); i++) {
                View maybeScrollable = commentOverflow.getChildAt(i);
                if (maybeScrollable instanceof HorizontalScrollView) {
                    sidebar.addScrollable(maybeScrollable);
                }
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

    private void setViews(String rawHTML, String subredditName, ProfileCommentViewHolder holder) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            holder.content.setVisibility(View.VISIBLE);
            holder.content.setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        } else {
            holder.content.setText("");
            holder.content.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                holder.overflow.setViews(blocks, subredditName);
            } else {
                holder.overflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
        } else {
            holder.overflow.removeAllViews();
        }
    }


}
