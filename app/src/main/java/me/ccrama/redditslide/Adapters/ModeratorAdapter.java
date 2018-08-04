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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
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
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CreateCardView;
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

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
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
                                    view.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();
                        }
                    });


                    submissions[0].saved = false;
                    v = null;
                } else {
                    new AccountManager(Authentication.reddit).save(submissions[0]);
                    final Snackbar s = Snackbar.make(v, R.string.submission_info_saved, Snackbar.LENGTH_SHORT);
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View view = s.getView();
                            TextView tv =
                                    view.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();
                        }
                    });


                    submissions[0].saved = true;
                    v = null;
                }
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
                    title.setText(Html.fromHtml(submission.getTitle()));

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
                            final Contribution old = dataSet.posts.get(pos);
                            dataSet.posts.remove(submission);
                            notifyItemRemoved(pos + 1);
                            d.dismiss();

                            Hidden.setHidden(old);

                            Snackbar s = Snackbar.make(listView, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dataSet.posts.add(pos, (PublicContribution) old);
                                    notifyItemInserted(pos + 1);
                                    Hidden.undoHidden(old);

                                }
                            });
                            View view = s.getView();
                            TextView tv =
                                    view.findViewById(android.support.design.R.id.snackbar_text);
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

            if (comment.getTimesGilded() > 0) {
                holder.gild.setVisibility(View.VISIBLE);
                ((TextView) holder.gild).setText(Integer.toString(comment.getTimesGilded()));
            } else if (holder.gild.getVisibility() == View.VISIBLE)
                holder.gild.setVisibility(View.GONE);

            if (comment.getSubmissionTitle() != null)
                holder.title.setText(Html.fromHtml(comment.getSubmissionTitle()));
            else
                holder.title.setText(Html.fromHtml(comment.getAuthor()));


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
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        final Drawable report = mContext.getResources().getDrawable(R.drawable.report);
        final Drawable approve = mContext.getResources().getDrawable(R.drawable.support);
        final Drawable nsfw = mContext.getResources().getDrawable(R.drawable.hide);
        final Drawable pin = mContext.getResources().getDrawable(R.drawable.sub);
        final Drawable distinguish = mContext.getResources().getDrawable(R.drawable.iconstarfilled);
        final Drawable remove = mContext.getResources().getDrawable(R.drawable.close);
        final Drawable ban = mContext.getResources().getDrawable(R.drawable.ban);
        final Drawable spam = mContext.getResources().getDrawable(R.drawable.spam);

        //Tint drawables
        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        report.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        approve.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        nsfw.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        distinguish.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        remove.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        pin.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        ban.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        spam.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();

        //Bottom sheet builder
        BottomSheet.Builder b = new BottomSheet.Builder((Activity) mContext).title(
                Html.fromHtml(comment.getBody()));

        int reportCount = reports.size() + reports2.size();

        if (reportCount == 0) {
            b.sheet(0, report, mContext.getString(R.string.mod_no_reports));
        } else {
            b.sheet(0, report, mContext.getResources()
                    .getQuantityString(R.plurals.mod_btn_reports, reportCount, reportCount));
        }

        b.sheet(1, approve, mContext.getString(R.string.mod_btn_approve));
        // b.sheet(2, spam, mContext.getString(R.string.mod_btn_spam)) todo this


        final boolean distinguished = !comment.getDataNode().get("distinguished").isNull();
        if (comment.getAuthor().equalsIgnoreCase(Authentication.name)) {
            if (!distinguished) {
                b.sheet(9, distinguish, mContext.getString(R.string.mod_distinguish));
            } else {
                b.sheet(9, distinguish, mContext.getString(R.string.mod_undistinguish));
            }
        }

        b.sheet(23, ban, mContext.getString(R.string.mod_ban_user));

        b.sheet(6, remove, mContext.getString(R.string.btn_remove))
                .sheet(10, spam, "Mark as spam")
                .sheet(8, profile, mContext.getString(R.string.mod_btn_author))
                .listener(new DialogInterface.OnClickListener() {
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
                                removeComment(mContext, holder, comment,  false);
                                break;
                            case 10:
                                removeComment(mContext, holder, comment,  true);
                                break;
                            case 8:
                                Intent i = new Intent(mContext, Profile.class);
                                i.putExtra(Profile.EXTRA_PROFILE, comment.getAuthor());
                                mContext.startActivity(i);
                                break;
                            case 23:
                                CommentAdapterHelper.showBan(mContext, holder.itemView, comment, "", "", "", "");
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
                    new ModerationManager(Authentication.reddit).remove(comment, spam);
                } catch (ApiException e) {
                    e.printStackTrace();
                    return false;

                }
                return true;
            }
        }.execute();
    }

}
