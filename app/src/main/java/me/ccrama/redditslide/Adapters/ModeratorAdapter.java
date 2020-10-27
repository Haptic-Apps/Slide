package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.bottomsheet.BottomSheet;
import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Toolbox.ToolboxUI;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SubmissionParser;


public class ModeratorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter {

    public static final int COMMENT = 1;
    private final int SPACER = 6;
    public static final int MESSAGE = 2;
    public static final int POST = 3;
    public final Activity mContext;
    private final RecyclerView listView;
    public ModeratorPosts dataSet;

    public ModeratorAdapter(Activity mContext, ModeratorPosts dataSet, RecyclerView listView) {
        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;
    }

    @Override
    public void setError(Boolean b) {
        listView.setAdapter(new ErrorAdapter());
    }

    @Override
    public void undoSetError() {
        listView.setAdapter(this);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && !dataSet.posts.isEmpty()) {
            return SPACER;
        } else if (!dataSet.posts.isEmpty()) {
            position -= 1;
        }

        if (dataSet.posts.get(position).getFullName().startsWith("t1"))//IS COMMENT
            return COMMENT;
        if (dataSet.posts.get(position).getFullName().startsWith("t4"))//IS MESSAGE
            return MESSAGE;
        return POST;
    }

    public static class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == SPACER) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spacer, viewGroup, false);
            return new SpacerViewHolder(v);

        } else if (i == MESSAGE) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.top_level_message, viewGroup, false);
            return new MessageViewHolder(v);
        }
        if (i == COMMENT) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_comment, viewGroup, false);
            return new ProfileCommentViewHolder(v);
        } else {
            View v = CreateCardView.CreateView(viewGroup);
            return new SubmissionViewHolder(v);

        }


    }

    public class AsyncSave extends AsyncTask<Submission, Void, Void> {
        View v;

        public AsyncSave(View v) {
            this.v = v;
        }

        @Override
        protected Void doInBackground(Submission... submissions) {
            try {
                if (ActionStates.isSaved(submissions[0])) {
                    new AccountManager(Authentication.reddit).unsave(submissions[0]);
                    final Snackbar s = Snackbar.make(v, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT);
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View view = s.getView();
                            TextView tv =
                                    view.findViewById(com.google.android.material.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();
                        }
                    });


                    submissions[0].saved = false;
                } else {
                    new AccountManager(Authentication.reddit).save(submissions[0]);
                    final Snackbar s = Snackbar.make(v, R.string.submission_info_saved, Snackbar.LENGTH_SHORT);
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View view = s.getView();
                            TextView tv =
                                    view.findViewById(com.google.android.material.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();
                        }
                    });


                    submissions[0].saved = true;
                }
                v = null;
            } catch (Exception e) {
                return null;
            }
            return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHold, final int pos) {
        int i = pos != 0 ? pos - 1 : pos;

        if (firstHold instanceof SubmissionViewHolder) {
            SubmissionViewHolder holder = (SubmissionViewHolder) firstHold;
            final Submission submission = (Submission) dataSet.posts.get(i);
            CreateCardView.resetColorCard(holder.itemView);
            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(Locale.ENGLISH), holder.itemView, "no_subreddit", false);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    LayoutInflater inflater = mContext.getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                    final TextView title = dialoglayout.findViewById(R.id.title);
                    title.setText(HtmlCompat.fromHtml(submission.getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));

                    ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                    ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                    dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, Profile.class);
                            i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                            mContext.startActivity(i);
                        }
                    });


                    dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, SubredditView.class);
                            i.putExtra(SubredditView.EXTRA_SUBREDDIT, submission.getSubredditName());
                            mContext.startActivity(i);
                        }
                    });

                    dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (submission.isSaved()) {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_save);
                            } else {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);

                            }
                            new AsyncSave(firstHold.itemView).execute(submission);

                        }
                    });
                    dialoglayout.findViewById(R.id.copy).setVisibility(View.GONE);
                    if (submission.isSaved()) {
                        ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);
                    }
                    dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String urlString = "https://reddit.com" + submission.getPermalink();
                            Intent i = new Intent(mContext, Website.class);
                            i.putExtra(LinkUtil.EXTRA_URL, urlString);
                            mContext.startActivity(i);
                        }
                    });
                    dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (submission.isSelfPost()){
                                if(SettingValues.shareLongLink){
                                    Reddit.defaultShareText("", "https://reddit.com" + submission.getPermalink(), mContext);
                                } else {
                                    Reddit.defaultShareText("", "https://redd.it/" + submission.getId(), mContext);
                                }
                            }
                            else {
                                new BottomSheet.Builder(mContext)
                                        .title(R.string.submission_share_title)
                                        .grid()
                                        .sheet(R.menu.share_menu)
                                        .listener(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case R.id.reddit_url:
                                                        if(SettingValues.shareLongLink){
                                                            Reddit.defaultShareText(submission.getTitle(), "https://reddit.com" + submission.getPermalink(), mContext);
                                                        } else {
                                                            Reddit.defaultShareText(submission.getTitle(), "https://redd.it/" + submission.getId(), mContext);
                                                        }
                                                        break;
                                                    case R.id.link_url:
                                                        Reddit.defaultShareText(submission.getTitle(), submission.getUrl(), mContext);
                                                        break;
                                                }
                                            }
                                        }).show();
                            }
                        }
                    });
                    if (!Authentication.isLoggedIn || !Authentication.didOnline) {
                        dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);
                        dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                    }
                    title.setBackgroundColor(Palette.getColor(submission.getSubredditName()));

                    builder.setView(dialoglayout);
                    final Dialog d = builder.show();
                    dialoglayout.findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final int pos = dataSet.posts.indexOf(submission);
                            final PublicContribution old = dataSet.posts.get(pos);
                            dataSet.posts.remove(submission);
                            notifyItemRemoved(pos + 1);
                            d.dismiss();

                            Hidden.setHidden(old);

                            Snackbar s = Snackbar.make(listView, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dataSet.posts.add(pos, old);
                                    notifyItemInserted(pos + 1);
                                    Hidden.undoHidden(old);

                                }
                            });
                            View view = s.getView();
                            TextView tv =
                                    view.findViewById(com.google.android.material.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();


                        }
                    });
                    return true;
                }
            });
            new PopulateSubmissionViewHolder().populateSubmissionViewHolder(holder, submission, mContext, false, false, dataSet.posts, listView, false, false, null, null);

            final ImageView hideButton = holder.itemView.findViewById(R.id.hide);
            if (hideButton != null) {
                hideButton.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "www.reddit.com" + submission.getPermalink();
                    url = url.replace("?ref=search_posts", "");
                    new OpenRedditLink(mContext, url);
                }
            });


        } else if (firstHold instanceof ProfileCommentViewHolder) {
            //IS COMMENT
            final ProfileCommentViewHolder holder = (ProfileCommentViewHolder) firstHold;
            final Comment comment = (Comment) dataSet.posts.get(i);

            SpannableStringBuilder author = new SpannableStringBuilder(comment.getAuthor());
            final int authorcolor = Palette.getFontColorUser(comment.getAuthor());

            if (comment.getDistinguishedStatus() == DistinguishedStatus.ADMIN) {
                author.replace(0, author.length(), " " + comment.getAuthor() + " ");
                author.setSpan(
                        new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_red_300, false),
                        0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (comment.getDistinguishedStatus() == DistinguishedStatus.SPECIAL) {
                author.replace(0, author.length(), " " + comment.getAuthor() + " ");
                author.setSpan(
                        new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_red_500, false),
                        0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (comment.getDistinguishedStatus() == DistinguishedStatus.MODERATOR) {
                author.replace(0, author.length(), " " + comment.getAuthor() + " ");
                author.setSpan(
                        new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false),
                        0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (Authentication.name != null && comment.getAuthor()
                    .toLowerCase(Locale.ENGLISH)
                    .equals(Authentication.name.toLowerCase(Locale.ENGLISH))) {
                author.replace(0, author.length(), " " + comment.getAuthor() + " ");
                author.setSpan(
                        new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_300,
                                false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (authorcolor != 0) {
                author.setSpan(new ForegroundColorSpan(authorcolor), 0, author.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            ToolboxUI.appendToolboxNote(mContext, author, comment.getSubredditName(), comment.getAuthor());

            holder.user.setText(author);
            holder.user.append(mContext.getResources().getString(R.string.submission_properties_seperator));
            holder.user.setVisibility(View.VISIBLE);

            holder.score.setText(comment.getScore() + " " + mContext.getResources().getQuantityString(R.plurals.points, comment.getScore()));

            if (Authentication.isLoggedIn) {
                if (ActionStates.getVoteDirection(comment) == VoteDirection.UPVOTE) {
                    holder.score.setTextColor(mContext.getResources().getColor(R.color.md_orange_500));
                } else if (ActionStates.getVoteDirection(comment) == VoteDirection.DOWNVOTE) {
                    holder.score.setTextColor(mContext.getResources().getColor(R.color.md_blue_500));
                } else {
                    holder.score.setTextColor(holder.time.getCurrentTextColor());
                }
            }
            String spacer = mContext.getString(R.string.submission_properties_seperator);
            SpannableStringBuilder titleString = new SpannableStringBuilder();


            String timeAgo = TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext);
            String time = ((timeAgo == null || timeAgo.isEmpty()) ? "just now" : timeAgo); //some users were crashing here
            time = time + (((comment.getEditDate() != null) ? " (edit " + TimeUtils.getTimeAgo(comment.getEditDate().getTime(), mContext) + ")" : ""));
            titleString.append(time);
            titleString.append(spacer);

            {
                final ImageView mod = holder.itemView.findViewById(R.id.mod);
                try {
                    if (UserSubscriptions.modOf.contains(comment.getSubredditName())) {
                        //todo
                        mod.setVisibility(View.GONE);

                    } else {
                        mod.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.d(LogUtil.getTag(), "Error loading mod " + e.toString());
                }
            }

            if ((UserSubscriptions.modOf != null) && UserSubscriptions.modOf.contains(
                    comment.getSubredditName().toLowerCase(Locale.ENGLISH))) {
                holder.itemView.findViewById(R.id.mod).setVisibility(View.VISIBLE);
                final Map<String, Integer> reports = comment.getUserReports();
                final Map<String, String> reports2 = comment.getModeratorReports();
                if (reports.size() + reports2.size() > 0) {
                    ((ImageView) holder.itemView.findViewById(R.id.mod)).setColorFilter(
                            ContextCompat.getColor(mContext, R.color.md_red_300),
                            PorterDuff.Mode.SRC_ATOP);
                } else {
                    int[] attrs = new int[]{R.attr.tintColor};
                    TypedArray ta = mContext.obtainStyledAttributes(attrs);
                    int color = ta.getColor(0, Color.WHITE);
                    ((ImageView)holder.itemView.findViewById(R.id.mod)).setColorFilter(color,
                            PorterDuff.Mode.SRC_ATOP);
                    ta.recycle();
                }
                holder.itemView.findViewById(R.id.mod).setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        showModBottomSheet(mContext,
                                comment, holder, reports, reports2);
                    }
                });
            } else {
                holder.itemView.findViewById(R.id.mod).setVisibility(View.GONE);
            }
            if (comment.getSubredditName() != null) {
                String subname = comment.getSubredditName();
                SpannableStringBuilder subreddit = new SpannableStringBuilder("/r/" + subname);
                if ((SettingValues.colorSubName && Palette.getColor(subname) != Palette.getDefaultColor())) {
                    subreddit.setSpan(new ForegroundColorSpan(Palette.getColor(subname)), 0, subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    subreddit.setSpan(new StyleSpan(Typeface.BOLD), 0, subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                titleString.append(subreddit);
            }

            holder.time.setText(titleString);
            setViews(comment.getDataNode().get("body_html").asText(), comment.getSubredditName(), holder);

            ((TextView) holder.gild).setText("");
            if (!SettingValues.hideCommentAwards && (comment.getTimesSilvered() > 0 || comment.getTimesGilded() > 0  || comment.getTimesPlatinized() > 0)) {
                TypedArray a = mContext.obtainStyledAttributes(
                        new FontPreferences(mContext).getPostFontStyle().getResId(),
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
                    Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.silver);
                    float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
                    image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                            (int) Math.ceil(fontsize), true);
                    silvered.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
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
                    Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.gold);
                    float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
                    image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                            (int) Math.ceil(fontsize), true);
                    gilded.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
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
                    Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.platinum);
                    float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
                    image = Bitmap.createScaledBitmap(image, (int) Math.ceil(fontsize * aspectRatio),
                            (int) Math.ceil(fontsize), true);
                    platinized.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    platinized.setSpan(new RelativeSizeSpan(0.75f), 3, platinized.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ((TextView) holder.gild).append(platinized);
                }
            } else if (holder.gild.getVisibility() == View.VISIBLE)
                holder.gild.setVisibility(View.GONE);

            if (comment.getSubmissionTitle() != null)
                holder.title.setText(HtmlCompat.fromHtml(comment.getSubmissionTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));
            else
                holder.title.setText(HtmlCompat.fromHtml(comment.getAuthor(), HtmlCompat.FROM_HTML_MODE_LEGACY));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OpenRedditLink(mContext, comment.getSubmissionId(), comment.getSubredditName(), comment.getId());
                }
            });
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OpenRedditLink(mContext, comment.getSubmissionId(), comment.getSubredditName(), comment.getId());
                }
            });

        }
        if (firstHold instanceof SpacerViewHolder) {
            firstHold.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(firstHold.itemView.getWidth(), mContext.findViewById(R.id.header).getHeight()));
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


    @Override
    public int getItemCount() {
        if (dataSet.posts == null || dataSet.posts.isEmpty()) {
            return 0;
        } else {
            return dataSet.posts.size() + 1;
        }
    }
    public static void showModBottomSheet(final Context mContext,
           final Comment comment, final ProfileCommentViewHolder holder,
            final Map<String, Integer> reports, final Map<String, String> reports2) {

        int[] attrs = new int[]{R.attr.tintColor};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        //Initialize drawables
        int color = ta.getColor(0, Color.WHITE);
        final Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        final Drawable report = mContext.getResources().getDrawable(R.drawable.report);
        final Drawable approve = mContext.getResources().getDrawable(R.drawable.support);
        final Drawable nsfw = mContext.getResources().getDrawable(R.drawable.hide);
        final Drawable pin = mContext.getResources().getDrawable(R.drawable.sub);
        final Drawable distinguish = mContext.getResources().getDrawable(R.drawable.star);
        final Drawable remove = mContext.getResources().getDrawable(R.drawable.close);
        final Drawable ban = mContext.getResources().getDrawable(R.drawable.ban);
        final Drawable spam = mContext.getResources().getDrawable(R.drawable.spam);
        final Drawable note = mContext.getResources().getDrawable(R.drawable.note);
        final Drawable removeReason = mContext.getResources().getDrawable(R.drawable.report_reason);
        final Drawable lock = mContext.getResources().getDrawable(R.drawable.lock);

        //Tint drawables
        profile.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        report.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        approve.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        nsfw.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        distinguish.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        remove.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        pin.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        ban.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        spam.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        note.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        removeReason.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        lock.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));

        ta.recycle();

        //Bottom sheet builder
        BottomSheet.Builder b = new BottomSheet.Builder((Activity) mContext).title(
                HtmlCompat.fromHtml(comment.getBody(), HtmlCompat.FROM_HTML_MODE_LEGACY));

        int reportCount = reports.size() + reports2.size();

        if (reportCount == 0) {
            b.sheet(0, report, mContext.getString(R.string.mod_no_reports));
        } else {
            b.sheet(0, report, mContext.getResources()
                    .getQuantityString(R.plurals.mod_btn_reports, reportCount, reportCount));
        }

        if (SettingValues.toolboxEnabled) {
            b.sheet(24, note, mContext.getString(R.string.mod_usernotes_view));
        }

        b.sheet(1, approve, mContext.getString(R.string.mod_btn_approve));
        b.sheet(6, remove, mContext.getString(R.string.btn_remove));
        b.sheet(7, removeReason, mContext.getString(R.string.mod_btn_remove_reason));
        b.sheet(10, spam, mContext.getString(R.string.mod_btn_spam));

        final boolean locked = comment.getDataNode().has("locked")
                && comment.getDataNode().get("locked").asBoolean();
        if (locked) {
            b.sheet(25, lock, mContext.getString(R.string.mod_btn_unlock_comment));
        } else {
            b.sheet(25, lock, mContext.getString(R.string.mod_btn_lock_comment));
        }

        final boolean distinguished = !comment.getDataNode().get("distinguished").isNull();
        if (comment.getAuthor().equalsIgnoreCase(Authentication.name)) {
            if (!distinguished) {
                b.sheet(9, distinguish, mContext.getString(R.string.mod_distinguish));
            } else {
                b.sheet(9, distinguish, mContext.getString(R.string.mod_undistinguish));
            }
        }

        b.sheet(8, profile, mContext.getString(R.string.mod_btn_author));
        b.sheet(23, ban, mContext.getString(R.string.mod_ban_user));

        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        CommentAdapterHelper.viewReports(mContext, reports, reports2);
                        break;
                    case 1:
                        doApproval(mContext, holder, comment);
                        break;
                    case 9:
                        if (distinguished) {
                            unDistinguishComment(mContext, holder, comment);
                        } else {
                            distinguishComment(mContext, holder, comment);
                        }
                        break;
                    case 6:
                        removeComment(mContext, holder, comment, false);
                        break;
                    case 7:
                        if (SettingValues.removalReasonType == SettingValues.RemovalReasonType.TOOLBOX.ordinal()
                                && ToolboxUI.canShowRemoval(comment.getSubredditName())) {
                            ToolboxUI.showRemoval(mContext, comment, new ToolboxUI.CompletedRemovalCallback() {
                                @Override
                                public void onComplete(boolean success) {
                                    if (success) {
                                        Snackbar s = Snackbar.make(holder.itemView, R.string.comment_removed,
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
                        } else { // Show a Slide reason dialog if we can't show a toolbox or reddit reason
                            doRemoveCommentReason(mContext, holder, comment);
                        }
                        break;
                    case 10:
                        removeComment(mContext, holder, comment, true);
                        break;
                    case 8:
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra(Profile.EXTRA_PROFILE, comment.getAuthor());
                        mContext.startActivity(i);
                        break;
                    case 23:
                        CommentAdapterHelper.showBan(mContext, holder.itemView, comment, "", "", "", "");
                        break;
                    case 24:
                        ToolboxUI.showUsernotes(mContext, comment.getAuthor(), comment.getSubredditName(),
                                "l," + comment.getParentId() + "," + comment.getId());
                        break;
                    case 25:
                        lockUnlockComment(mContext, holder, comment, !locked);
                        break;
                }
            }
        });
        b.show();
    }

    public static void doApproval(final Context mContext, final ProfileCommentViewHolder holder,
            final Comment comment) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar.make(holder.itemView, R.string.mod_approved, Snackbar.LENGTH_LONG)
                            .show();

                } else {
                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                            .setMessage(R.string.err_retry_later)
                            .show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    new ModerationManager(Authentication.reddit).approve(comment);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.execute();
    }

    public static void distinguishComment(final Context mContext, final ProfileCommentViewHolder holder,
            final Comment comment) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.comment_distinguished,
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
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(comment,
                            DistinguishedStatus.MODERATOR);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.execute();
    }

    public static void unDistinguishComment(final Context mContext, final ProfileCommentViewHolder holder,
            final Comment comment) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.comment_undistinguished,
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
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(comment,
                            DistinguishedStatus.NORMAL);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.execute();
    }

    public static void removeComment(final Context mContext, final ProfileCommentViewHolder holder,
            final Comment comment, final boolean spam) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.comment_removed,
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
                    new ModerationManager(Authentication.reddit).remove(comment, spam);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.execute();
    }

    /**
     * Show a removal dialog to input a reason, then remove comment and post reason
     * @param mContext context
     * @param holder commentviewholder
     * @param comment comment
     */
    public static void doRemoveCommentReason(final Context mContext, final ProfileCommentViewHolder holder,
            final Comment comment) {
        new MaterialDialog.Builder(mContext).title(R.string.mod_remove_title)
                .positiveText(R.string.btn_remove)
                .alwaysCallInputCallback()
                .input(mContext.getString(R.string.mod_remove_hint),
                        mContext.getString(R.string.mod_remove_template), false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
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
                        removeCommentReason(comment, mContext, holder, dialog.getInputEditText().getText().toString());
                    }
                })
                .negativeText(R.string.btn_cancel)
                .show();
    }

    /**
     * Remove a comment and post a reason
     * @param comment comment
     * @param mContext context
     * @param holder commentviewholder
     * @param reason reason
     */
    public static void removeCommentReason(final Comment comment, final Context mContext,
            ProfileCommentViewHolder holder, final String reason) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, R.string.comment_removed, Snackbar.LENGTH_LONG);
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
                    new AccountManager(Authentication.reddit).reply(comment, reason);
                    new ModerationManager(Authentication.reddit).remove(comment, false);
                    new ModerationManager(Authentication.reddit).setDistinguishedStatus(
                            Authentication.reddit.get(comment.getFullName()).get(0),
                            DistinguishedStatus.MODERATOR);
                } catch (ApiException | NetworkException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void lockUnlockComment(final Context mContext, final ProfileCommentViewHolder holder,
            final Comment comment, final boolean lock) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            public void onPostExecute(Boolean b) {
                if (b) {
                    Snackbar s = Snackbar.make(holder.itemView, lock ? R.string.mod_locked : R.string.mod_unlocked,
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
                    if (lock) {
                        new ModerationManager(Authentication.reddit).setLocked(comment);
                    } else {
                        new ModerationManager(Authentication.reddit).setUnlocked(comment);
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.execute();
    }

}
