package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.Vote;
import me.ccrama.redditslide.util.LogUtil;


public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static int HEADER = 1;
    public Context mContext;
    public SubmissionComments dataSet;
    public Submission submission;
    public int currentlyHighlighted;
    public CommentViewHolder currentlySelected;
    public String currentSelectedItem = "";
    public int shiftFrom;
    public FragmentManager fm;
    public int clickpos;
    public int currentPos;
    public CommentViewHolder isHolder;
    public boolean isClicking;
    public HashMap<String, Integer> keys = new HashMap<>();
    public ArrayList<CommentObject> users;
    public ArrayList<String> deleted = new ArrayList<>();
    RecyclerView listView;
    ArrayList<String> up;
    ArrayList<String> down;
    CommentPage mPage;
    boolean isSame;
    int shifted;
    int toShiftTo;
    ArrayList<String> hidden;
    ArrayList<String> hiddenPersons;
    ArrayList<String> replie;

    public CommentAdapter(CommentPage mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission, FragmentManager fm) {

        this.mContext = mContext.getContext();
        mPage = mContext;
        this.listView = listView;
        this.dataSet = dataSet;
        this.fm = fm;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        replie = new ArrayList<>();
        up = new ArrayList<>();
        down = new ArrayList<>();

        shifted = 0;

        isSame = false;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (i == HEADER) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_fullscreen, viewGroup, false);
            return new SubmissionViewHolder(v);
        } else if (i == 2) {

            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment, viewGroup, false);
            return new CommentViewHolder(v);

        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.morecomment, viewGroup, false);
            return new MoreCommentViewHolder(v);

        }

    }

    public void reset(Context mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        replie = new ArrayList<>();


        notifyItemRangeChanged(1, users.size() + 1);
        isSame = false;


        if (currentSelectedItem != null && !currentSelectedItem.isEmpty()) {
            int i = 1;
            for (CommentObject n : users) {

                if (n.getName().contains(currentSelectedItem) && !(n instanceof MoreChildItem)) {
                    RecyclerView.SmoothScroller smoothScroller = new CommentPage.TopSnappedSmoothScroller(listView.getContext(), (PreCachingLayoutManagerComments) listView.getLayoutManager());
                    smoothScroller.setTargetPosition(i);
                    (listView.getLayoutManager()).startSmoothScroll(smoothScroller);
                    break;
                }
                i++;
            }
        }

    }

    public void reset(Context mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission, int oldSize) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        replie = new ArrayList<>();


        isSame = false;
        notifyDataSetChanged();
        if (currentSelectedItem != null && !currentSelectedItem.isEmpty()) {
            int i = 1;

            for (CommentObject n : users) {

                if (n.getName().contains(currentSelectedItem) && !(n instanceof MoreChildItem)) {
                    RecyclerView.SmoothScroller smoothScroller = new CommentPage.TopSnappedSmoothScroller(listView.getContext(), (PreCachingLayoutManagerComments) listView.getLayoutManager());
                    smoothScroller.setTargetPosition(i);
                    (listView.getLayoutManager()).startSmoothScroll(smoothScroller);
                    break;
                }
                i++;
            }
        }
    }

    public void setError(boolean b) {
        listView.setAdapter(new ErrorAdapter());
    }

    public void doHighlighted(final CommentViewHolder holder, final Comment n, final CommentNode baseNode, final int finalPos, final int finalPos1) {
        if (currentlySelected != null) {
            doUnHighlighted(currentlySelected);
        }
        currentlySelected = holder;
        holder.dots.setVisibility(View.GONE);
        int color = Palette.getColor(n.getSubredditName());
        currentSelectedItem = n.getFullName();

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        final View baseView = inflater.inflate(R.layout.comment_menu, holder.menuArea);

        View reply = baseView.findViewById(R.id.reply);
        View send = baseView.findViewById(R.id.send);

        final View menu = baseView.findViewById(R.id.menu);
        final View replyArea = baseView.findViewById(R.id.replyArea);

        final View more = baseView.findViewById(R.id.more);
        final ImageView upvote = (ImageView) baseView.findViewById(R.id.upvote);
        final ImageView downvote = (ImageView) baseView.findViewById(R.id.downvote);
        View discard = baseView.findViewById(R.id.discard);
        final EditText replyLine = (EditText) baseView.findViewById(R.id.replyLine);

        String scoreText;
        if (n.isScoreHidden())
            scoreText = "[" + mContext.getString(R.string.misc_score_hidden).toUpperCase() + "]";
        else scoreText = n.getScore().toString();

        holder.score.setText(scoreText);

        if (up.contains(n.getFullName())) {
            holder.score.setTextColor(holder.textColorUp);
            upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
        } else if (down.contains(n.getFullName())) {
            holder.score.setTextColor(holder.textColorDown);
            downvote.setColorFilter(holder.textColorDown, PorterDuff.Mode.MULTIPLY);
        } else {
            holder.score.setTextColor(holder.textColorRegular);
            downvote.clearColorFilter();
            upvote.clearColorFilter();
        }
        {
            final ImageView mod = (ImageView) baseView.findViewById(R.id.mod);
            try {
                if (SubredditStorage.modOf.contains(submission.getSubredditName())) {
                    //todo
                    mod.setVisibility(View.GONE);

                } else {
                    mod.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Log.d(LogUtil.getTag(), "Error loading mod " + e.toString());
            }
        }
        {
            final ImageView edit = (ImageView) baseView.findViewById(R.id.edit);
            if (Authentication.name.toLowerCase().equals(baseNode.getComment().getAuthor().toLowerCase())) {
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

                        final View dialoglayout = inflater.inflate(R.layout.edit_comment, null);
                        final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);

                        final EditText e = (EditText) dialoglayout.findViewById(R.id.entry);
                        e.setText(StringEscapeUtils.unescapeHtml4(baseNode.getComment().getBody()));


                        builder.setView(dialoglayout);
                        final Dialog d = builder.create();
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
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        try {
                                            new AccountManager(Authentication.reddit).updateContribution(baseNode.getComment(), e.getText().toString());
                                            dataSet.loadMore(CommentAdapter.this, submission.getSubredditName());


                                            currentSelectedItem = baseNode.getComment().getFullName();
                                            d.dismiss();
                                        } catch (Exception e) {
                                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new AlertDialogWrapper.Builder(mContext)
                                                            .setTitle(R.string.comment_delete_err)
                                                            .setMessage(R.string.comment_delete_err_msg)
                                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                    doInBackground();
                                                                }
                                                            }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    }).show();
                                                }
                                            });
                                        }
                                        return null;
                                    }
                                }.execute();
                            }
                        });


                    }
                });
            } else {
                edit.setVisibility(View.GONE);
            }
        }
        {
            final ImageView delete = (ImageView) baseView.findViewById(R.id.delete);
            if (Authentication.name.toLowerCase().equals(baseNode.getComment().getAuthor().toLowerCase())) {
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AlertDialogWrapper.Builder(mContext)
                                .setTitle(R.string.comment_delete)
                                .setMessage(R.string.comment_delete_msg)
                                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new AsyncTask<Void, Void, Void>() {

                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                try {
                                                    new ModerationManager(Authentication.reddit).delete(baseNode.getComment());
                                                    deleted.add(baseNode.getComment().getFullName());

                                                    ((Activity) mContext).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            holder.content.setText("[deleted]");
                                                            holder.author.setText("[deleted]");
                                                        }
                                                    });

                                                } catch (ApiException e) {
                                                    ((Activity) mContext).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            new AlertDialogWrapper.Builder(mContext)
                                                                    .setTitle(R.string.comment_delete_err)
                                                                    .setMessage(R.string.comment_delete_err_msg)
                                                                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            dialog.dismiss();
                                                                            doInBackground();
                                                                        }
                                                                    }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                }
                                                            }).show();
                                                        }
                                                    });

                                                    e.printStackTrace();
                                                }

                                                return null;
                                            }
                                        }.execute();

                                    }
                                })
                                .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();

                        //todo delete
                    }
                });
            } else {
                delete.setVisibility(View.GONE);
            }
        }
        if (Authentication.isLoggedIn && !submission.isArchived() && Authentication.didOnline) {
            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replyArea.setVisibility(View.VISIBLE);
                    menu.setVisibility(View.GONE);
                    DoEditorActions.doActions(replyLine, replyArea, fm);

                }
            });
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replyArea.setVisibility(View.GONE);
                    menu.setVisibility(View.VISIBLE);
                    dataSet.refreshLayout.setRefreshing(true);
                    new ReplyTaskComment(n, finalPos, finalPos1, baseNode).execute(replyLine.getText().toString());

                    //Hide soft keyboard
                    View view = ((Activity) mContext).getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                }
            });
            discard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menu.setVisibility(View.VISIBLE);
                    replyArea.setVisibility(View.GONE);
                }
            });

        } else {

            if (reply.getVisibility() == View.VISIBLE)

                reply.setVisibility(View.GONE);
            if (upvote.getVisibility() == View.VISIBLE)

                upvote.setVisibility(View.GONE);
            if (downvote.getVisibility() == View.VISIBLE)

                downvote.setVisibility(View.GONE);

        }

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.commentmenu, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setText(Html.fromHtml(n.getBody()));

                ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + n.getAuthor());
                dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra(Profile.EXTRA_PROFILE, n.getAuthor());
                        mContext.startActivity(i);
                    }
                });
                final boolean[] saved = {n.isSaved()};
                if (saved[0]) {
                    ((TextView) dialoglayout.findViewById(R.id.save)).setText(R.string.comment_unsave);
                }
                dialoglayout.findViewById(R.id.save_body).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (saved[0]) {
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {
                                        new AccountManager(Authentication.reddit).unsave(n);
                                    } catch (ApiException e) {
                                        e.printStackTrace();
                                    }

                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    saved[0] = false;
                                    ((TextView) dialoglayout.findViewById(R.id.save)).setText(R.string.btn_save);
                                }
                            }.execute();


                        } else {
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {
                                        new AccountManager(Authentication.reddit).save(n);
                                    } catch (ApiException e) {
                                        e.printStackTrace();
                                    }

                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    ((TextView) dialoglayout.findViewById(R.id.save)).setText(R.string.comment_unsave);

                                    saved[0] = true;
                                }
                            }.execute();

                        }
                    }
                });

                dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String urlString = "https://reddit.com" + submission.getPermalink() +
                                n.getFullName().substring(3, n.getFullName().length()) + "?context=3";

                        OpenRedditLink.customIntentChooser(urlString, mContext);
                    }
                });
                dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String urlString = "https://reddit.com" + submission.getPermalink() + n.getFullName().substring(3, n.getFullName().length()) + "?context=3";
                        Reddit.defaultShareText(urlString, mContext);
                    }
                });

                dialoglayout.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Reddit post", n.getDataNode().get("body").asText());
                        clipboard.setPrimaryClip(clip);
                    }
                });

                if (!Authentication.isLoggedIn || !Authentication.didOnline) {

                    dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);
                    dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);

                }
                title.setBackgroundColor(Palette.getColor(submission.getSubredditName()));

                builder.setView(dialoglayout);
                builder.show();
            }
        });
        upvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (up.contains(n.getFullName())) {
                    new Vote(v, mContext).execute(n);
                    up.remove(n.getFullName());
                    holder.score.setTextColor(holder.textColorRegular);
                    if (!n.isScoreHidden()) {
                        holder.score.setText(n.getScore() + "");
                    }
                    upvote.clearColorFilter();

                } else if (down.contains(n.getFullName())) {
                    new Vote(true, v, mContext).execute(n);
                    up.add(n.getFullName());
                    if (!n.isScoreHidden()) {
                        holder.score.setText(n.getScore() + 1 + "");
                    }
                    down.remove(n.getFullName());
                    downvote.clearColorFilter(); // reset colour
                    holder.score.setTextColor(holder.textColorUp);
                    upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
                } else {
                    new Vote(true, v, mContext).execute(n);
                    if (!n.isScoreHidden()) {
                        holder.score.setText(n.getScore() + 1 + "");
                    }
                    up.add(n.getFullName());
                    holder.score.setTextColor(holder.textColorUp);
                    upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
                }
            }
        });
        downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (down.contains(n.getFullName())) {
                    new Vote(v, mContext).execute(n);
                    down.remove(n.getFullName());
                    holder.score.setTextColor(holder.textColorRegular);
                    if (!n.isScoreHidden()) {
                        holder.score.setText(n.getScore() + "");
                    }
                    downvote.clearColorFilter();

                } else if (up.contains(n.getFullName())) {
                    new Vote(false, v, mContext).execute(n);
                    down.add(n.getFullName());
                    up.remove(n.getFullName());
                    if (!n.isScoreHidden()) {
                        holder.score.setText(n.getScore() - 1 + "");
                    }
                    upvote.clearColorFilter(); // reset colour
                    holder.score.setTextColor(holder.textColorDown);
                    downvote.setColorFilter(holder.textColorDown);

                } else {
                    new Vote(false, v, mContext).execute(n);
                    if (!n.isScoreHidden()) {
                        holder.score.setText(n.getScore() - 1 + "");
                    }
                    down.add(n.getFullName());
                    holder.score.setTextColor(holder.textColorDown);
                    downvote.setColorFilter(holder.textColorDown);
                }
            }
        });
        menu.setBackgroundColor(color);
        replyArea.setBackgroundColor(color);

        menu.setVisibility(View.VISIBLE);
        replyArea.setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.background).setBackgroundColor(Color.argb(50, Color.red(color), Color.green(color), Color.blue(color)));
    }

    public void doUnHighlighted(CommentViewHolder holder) {

        holder.dots.setVisibility(View.VISIBLE);

        holder.menuArea.removeAllViews();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = mContext.getTheme();
        theme.resolveAttribute(R.attr.card_background, typedValue, true);
        int color = typedValue.data;
        holder.itemView.findViewById(R.id.background).setBackgroundColor(color);
    }

    public void doUnHighlighted(CommentViewHolder holder, Comment comment) {
        currentlySelected = null;
        currentSelectedItem = "";
        holder.menuArea.removeAllViews();
        holder.dots.setVisibility(View.VISIBLE);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = mContext.getTheme();
        theme.resolveAttribute(R.attr.card_background, typedValue, true);
        int color = typedValue.data;
        holder.itemView.findViewById(R.id.background).setBackgroundColor(color);
    }

    public void doLongClick(CommentViewHolder holder, Comment comment, CommentNode baseNode, int finalPos, int finalPos1) {
        if (currentSelectedItem.contains(comment.getFullName())) {
            doUnHighlighted(holder, comment);
        } else {
            doHighlighted(holder, comment, baseNode, finalPos, finalPos1);
        }
    }

    public void doOnClick(CommentViewHolder holder, Comment comment, CommentNode baseNode) {
        if (currentSelectedItem.contains(comment.getFullName())) {
            doUnHighlighted(holder, comment);
        } else {
            doOnClick(holder, baseNode, comment);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHolder, int pos) {

        if (firstHolder instanceof CommentViewHolder) {
            final CommentViewHolder holder = (CommentViewHolder) firstHolder;
            int nextPos = pos - 1;

            nextPos = getRealPosition(nextPos);
            final int finalPos = nextPos;
            final int finalPos1 = pos;


            if (pos > toShiftTo) {
                shifted = 0;
            }
            if (pos < shiftFrom) {
                shifted = 0;
            }
            final CommentNode baseNode = users.get(nextPos).comment;
            final Comment comment = baseNode.getComment();

            if (comment.getFullName().contains(currentSelectedItem) && !currentSelectedItem.isEmpty()) {
                doHighlighted(holder, comment, baseNode, finalPos, finalPos1);
            } else {
                doUnHighlighted(holder);
            }

            if (pos == getItemCount() - 1) {
                holder.itemView.setPadding(0, 0, 0, (int) mContext.getResources().getDimension(R.dimen.overview_top_padding_single));

            } else {
                holder.itemView.setPadding(0, 0, 0, 0);

            }

            if (comment.getVote() == VoteDirection.UPVOTE) {
                if (!up.contains(comment.getFullName())) {
                    up.add(comment.getFullName());
                }
            } else if (comment.getVote() == VoteDirection.DOWNVOTE) {
                if (!down.contains(comment.getFullName())) {
                    down.add(comment.getFullName());
                }
            }
            final CommentObject prev = users.get(nextPos);



                holder.commentArea.setVisibility(View.GONE);


            String scoreText;
            if (comment.isScoreHidden())
                scoreText = "[" + mContext.getString(R.string.misc_score_hidden).toUpperCase() + "]";
            else scoreText = comment.getScore().toString();

            holder.score.setText(scoreText);

            if (up.contains(comment.getFullName()))
                holder.score.setTextColor(holder.textColorUp);
            else if (down.contains(comment.getFullName()))
                holder.score.setTextColor(holder.textColorDown);
            else holder.score.setTextColor(holder.textColorRegular);


            if (comment.getAuthor().toLowerCase().equals(Authentication.name.toLowerCase())) {
                holder.you.setVisibility(View.VISIBLE);
            } else {
                if (holder.itemView.findViewById(R.id.you).getVisibility() == View.VISIBLE)
                    holder.you.setVisibility(View.GONE);

            }
            if (comment.getAuthor().toLowerCase().equals(submission.getAuthor().toLowerCase())) {
                holder.op.setVisibility(View.VISIBLE);
            } else {

                if (holder.op.getVisibility() == View.VISIBLE)

                    holder.op.setVisibility(View.GONE);

            }
            if(comment.getDataNode().get("stickied").asBoolean()){
                holder.itemView.findViewById(R.id.sticky).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.sticky).setVisibility(View.GONE);

            }

            String distingush = "";
            if (comment.getDistinguishedStatus() == DistinguishedStatus.MODERATOR)
                distingush = "[M]";
            else if (comment.getDistinguishedStatus() == DistinguishedStatus.ADMIN)
                distingush = "[A]";

            holder.author.setText(comment.getAuthor() + distingush);
            if (comment.getAuthorFlair() != null && comment.getAuthorFlair().getText() != null && !comment.getAuthorFlair().getText().isEmpty()) {
                holder.flairBubble.setVisibility(View.VISIBLE);
                holder.flairText.setText(Html.fromHtml(comment.getAuthorFlair().getText()));

            } else {

                if (holder.flairBubble.getVisibility() == View.VISIBLE)
                    holder.flairBubble.setVisibility(View.GONE);

            }
            holder.content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (SettingValues.swap) {
                        doOnClick(holder, comment, baseNode);
                    } else {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    }
                    return true;
                }
            });
            new MakeTextviewClickable().ParseTextWithLinksTextViewComment(comment.getDataNode().get("body_html").asText(), holder.content, (Activity) mContext, submission.getSubredditName());

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (SettingValues.swap) {
                        doOnClick(holder, comment, baseNode);

                    } else {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    }
                    return true;
                }
            });

            if (baseNode.isTopLevel()) {
                holder.itemView.findViewById(R.id.next).setVisibility(View.VISIBLE);
            } else {
                if (holder.itemView.findViewById(R.id.next).getVisibility() == View.VISIBLE)

                    holder.itemView.findViewById(R.id.next).setVisibility(View.GONE);

            }
            holder.time.setText(TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext));

            if (comment.getTimesGilded() > 0) {
                holder.gild.setVisibility(View.VISIBLE);
                ((TextView) holder.gild.findViewById(R.id.gildtext)).setText("" + comment.getTimesGilded());
            } else if (holder.gild.getVisibility() == View.VISIBLE)
                    holder.gild.setVisibility(View.GONE);

            if (hiddenPersons.contains(comment.getFullName())) {
                holder.children.setVisibility(View.VISIBLE);
                holder.childrenNumber.setText("+" + getChildNumber(baseNode));
                //todo maybe   holder.content.setVisibility(View.GONE);
            } else {
                holder.children.setVisibility(View.GONE);
                //todo maybe  holder.content.setVisibility(View.VISIBLE);

            }

            holder.author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SettingValues.click_user_name_to_profile) {
                        Intent i2 = new Intent(mContext, Profile.class);
                        i2.putExtra(Profile.EXTRA_PROFILE, comment.getAuthor());
                        mContext.startActivity(i2);
                    } else {
                        holder.itemView.performClick();
                    }
                }
            });
            holder.author.setTextColor(Palette.getFontColorUser(comment.getAuthor()));
            if (holder.author.getCurrentTextColor() == 0) {
                holder.author.setTextColor(holder.time.getCurrentTextColor());
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SettingValues.swap) {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    } else {
                        doOnClick(holder, comment, baseNode);
                    }
                }
            });
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SpoilerRobotoTextView SpoilerRobotoTextView = (SpoilerRobotoTextView) v;
                    if (SettingValues.swap) {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    } else if (!SpoilerRobotoTextView.isSpoilerClicked()) {
                        doOnClick(holder, comment, baseNode);
                    } else if (SpoilerRobotoTextView.isSpoilerClicked()) {
                        SpoilerRobotoTextView.resetSpoilerClicked();
                    }
                }
            });

            holder.dot.setVisibility(View.VISIBLE);


            int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
            int width = 0;

            //Padding on the left, starting with the third comment
            for (int i = 2; i < baseNode.getDepth(); i++) {
                width += dwidth;
            }
            holder.dots.setPadding(width, 0, 0, 0);

            if (baseNode.getDepth() - 1 > 0) {
                int i22 = baseNode.getDepth() - 2;
                if (i22 % 5 == 0) {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_blue_500));
                } else if (i22 % 4 == 0) {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_green_500));

                } else if (i22 % 3 == 0) {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_yellow_500));

                } else if (i22 % 2 == 0) {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_orange_500));

                } else {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, R.color.md_red_500));
                }
            } else {
                holder.dot.setVisibility(View.GONE);
            }
            if (deleted.contains(comment.getFullName())) {
                holder.content.setText(R.string.comment_deleted);
                holder.author.setText(R.string.comment_deleted);


            }
        } else if(firstHolder instanceof SubmissionViewHolder){
            new PopulateSubmissionViewHolder().populateSubmissionViewHolder((SubmissionViewHolder) firstHolder, submission, (Activity) mContext, true, true, null, null, false, false);
            if (Authentication.isLoggedIn && Authentication.didOnline) {
                if (submission.isArchived())
                    firstHolder.itemView.findViewById(R.id.reply).setVisibility(View.GONE);
                else {
                    firstHolder.itemView.findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final View replyArea = firstHolder.itemView.findViewById(R.id.innerSend);
                            if (replyArea.getVisibility() == View.GONE) {
                                replyArea.setVisibility(View.VISIBLE);
                                DoEditorActions.doActions(((EditText) firstHolder.itemView.findViewById(R.id.replyLine)), firstHolder.itemView, fm);

                                firstHolder.itemView.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dataSet.refreshLayout.setRefreshing(true);

                                        new ReplyTaskComment(submission).execute(((EditText) firstHolder.itemView.findViewById(R.id.replyLine)).getText().toString());
                                        replyArea.setVisibility(View.GONE);

                                        //Hide soft keyboard
                                        View view = ((Activity) mContext).getCurrentFocus();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }
                                    }
                                });
                            } else {
                                replyArea.setVisibility(View.GONE);
                            }
                        }
                    });
                    firstHolder.itemView.findViewById(R.id.discard).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);
                        }
                    });
                }
            } else {
                firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);
                firstHolder.itemView.findViewById(R.id.reply).setVisibility(View.GONE);

            }

            firstHolder.itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
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
                    dialoglayout.findViewById(R.id.hide).setVisibility(View.GONE);
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
                            if (submission.saved) {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_save);
                            } else {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);

                            }
                            new AsyncSave(firstHolder.itemView).execute(submission);

                        }
                    });
                    if (submission.saved) {
                        ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);
                    }
                    dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String urlString = "https://reddit.com" + submission.getPermalink();
                            OpenRedditLink.customIntentChooser(urlString, mContext);
                        }
                    });
                    dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (submission.isSelfPost())
                                Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), mContext);
                            else
                                new BottomSheet.Builder(mContext, R.style.BottomSheet_Dialog)
                                        .title(R.string.submission_share_title)
                                        .grid()
                                        .sheet(R.menu.share_menu)
                                        .listener(new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case R.id.reddit_url:
                                                        Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), mContext);
                                                        break;
                                                    case R.id.link_url:
                                                        Reddit.defaultShareText(submission.getUrl(), mContext);
                                                        break;
                                                }
                                            }
                                        }).show();
                        }
                    });

                    if (submission.isSelfPost() && !submission.getSelftext().isEmpty()) {
                        dialoglayout.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Reddit post", submission.getSelftext());
                                clipboard.setPrimaryClip(clip);
                            }
                        });
                    } else dialoglayout.findViewById(R.id.copy).setVisibility(View.GONE);

                    if (!Authentication.isLoggedIn || !Authentication.didOnline) {
                        dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);
                        dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                    }
                    title.setBackgroundColor(Palette.getColor(submission.getSubredditName()));

                    builder.setView(dialoglayout);
                    builder.show();
                }
            });
        } else {

            final MoreCommentViewHolder holder = (MoreCommentViewHolder) firstHolder;
            int nextPos = pos - 1;

            nextPos = getRealPosition(nextPos);

            final MoreChildItem baseNode = (MoreChildItem) users.get(nextPos);
            ((TextView) holder.itemView.findViewById(R.id.content)).setText(
                    mContext.getString(R.string.comment_load_more, baseNode.children.getCount()));



            int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
            int width = 0;
            for (int i = 1; i < baseNode.comment.getDepth(); i++) {
                width += dwidth;
            }

            final View progress = holder.itemView.findViewById(R.id.loading);
            progress.setVisibility(View.GONE);
            final int finalNextPos = nextPos;
            (holder.itemView.findViewById(R.id.content)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (progress.getVisibility() == View.GONE) {
                        progress.setVisibility(View.VISIBLE);
                        ((TextView) holder.itemView.findViewById(R.id.content)).setText(R.string.comment_loading_more);
                        new AsyncLoadMore(getRealPosition(holder.getAdapterPosition() - 1) , holder.getAdapterPosition() , holder).execute(baseNode);
                    }


                }
            });

            (holder.itemView.findViewById(R.id.dots)).setPadding(width, 0, 0, 0);
        }

    }

    public void doOnClick(CommentViewHolder holder, CommentNode baseNode, Comment comment) {
        if (isClicking) {
            isClicking = false;
            holder.menuArea.removeAllViews();
            isHolder.itemView.findViewById(R.id.menu).setVisibility(View.GONE);
        } else {
            if (hiddenPersons.contains(comment.getFullName())) {
                unhideAll(baseNode, holder.getAdapterPosition() + 1);
                hiddenPersons.remove(comment.getFullName());
                holder.children.setVisibility(View.GONE);
                //todo maybe holder.content.setVisibility(View.VISIBLE);
            } else {
                int childNumber = getChildNumber(baseNode);
                if (childNumber > 0) {
                    hideAll(baseNode, holder.getAdapterPosition() + 1);
                    hiddenPersons.add(comment.getFullName());
                    holder.children.setVisibility(View.VISIBLE);
                    ((TextView) holder.children.findViewById(R.id.flairtext)).setText("+" + childNumber);
                    //todo maybe holder.content.setVisibility(View.GONE);
                }
            }
            clickpos = holder.getAdapterPosition() + 1;
        }
    }

    private int getChildNumber(CommentNode user) {
        int i = 0;
        for (CommentNode ignored : user.walkTree()) {
            i++;
            if(ignored.hasMoreComments()){
                i++;
            }
        }

        return i - 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEADER;
        return (users.get(getRealPosition(position - 1)) instanceof CommentItem?2:3);

    }

    @Override
    public int getItemCount() {
        if (users == null) {
            return 1;
        } else {
            return 1 + (users.size() - getHiddenCount());
        }
    }

    private int getHiddenCount() {

        return hidden.size();
    }

    public void unhideAll(CommentNode n, int i) {
        int counter = unhideNumber(n, 0);
        listView.setItemAnimator(new ScaleInLeftAnimator());
        listView.setItemAnimator(new FadeInAnimator());

        notifyItemRangeInserted(i, counter);


    }

    public void hideAll(CommentNode n, int i) {
        int counter = hideNumber(n, 0);
        listView.setItemAnimator(new FadeInDownAnimator());

        notifyItemRangeRemoved(i, counter);

    }

    public int unhideNumber(CommentNode n, int i) {
        for (CommentNode ignored : n.walkTree()) {
            if (!ignored.getComment().getFullName().equals(n.getComment().getFullName())) {
                String name = ignored.getComment().getFullName();
                if (hiddenPersons.contains(name)) {
                    hiddenPersons.remove(name);
                }
                if (hidden.contains(name)) {
                    hidden.remove(name);
                    i++;
                }
                if(ignored.getMoreChildren() != null){
                    name = name + "more";
                    if (hiddenPersons.contains(name)) {
                        hiddenPersons.remove(name);
                    }
                    if (hidden.contains(name)) {
                        hidden.remove(name);
                        i++;
                    }
                }
                i += unhideNumber(ignored, 0);
            }
        }
        if(n.hasMoreComments()){
            String fullname  = n.getComment().getFullName() + "more";

            if (hidden.contains(fullname)) {
                i++;
                hidden.remove(fullname);

            }
        }
        return i;
    }

    public int hideNumber(CommentNode n, int i) {
        for (CommentNode ignored : n.walkTree()) {
            if (!ignored.getComment().getFullName().equals(n.getComment().getFullName())) {

                String fullname = ignored.getComment().getFullName();
                if (hiddenPersons.contains(fullname)) {
                    hiddenPersons.remove(fullname);
                }
                if (!hidden.contains(fullname)) {
                    i++;
                    hidden.add(fullname);

                }
                if(ignored.hasMoreComments()){
                    fullname  = fullname + "more";

                    if (!hidden.contains(fullname)) {
                        i++;
                        hidden.add(fullname);

                    }
                }
                i += hideNumber(ignored, 0);
            }
            if(n.hasMoreComments()){
                String fullname  = n.getComment().getFullName() + "more";

                if (!hidden.contains(fullname)) {
                    i++;
                    hidden.add(fullname);

                }
            }
        }
        return i;
    }

    public int getRealPosition(int position) {

        int hElements = getHiddenCountUpTo(position);
        int diff = 0;
        for (int i = 0; i < hElements; i++) {
            diff++;
            if (hidden.contains(users.get(position + diff).getName())) {
                i--;
            }
        }
        return (position + diff);
    }

    private int getHiddenCountUpTo(int location) {
        int count = 0;
        for (int i = 0; i <= location; i++) {
            if (hidden.contains(users.get(i).getName()))
                count++;
        }
        return count;
    }

    public class AsyncSave extends AsyncTask<Submission, Void, Void> {

        View v;

        public AsyncSave(View v) {
            this.v = v;
        }

        @Override
        protected Void doInBackground(Submission... submissions) {
            try {
                if (submissions[0].saved) {
                    new AccountManager(Authentication.reddit).unsave(submissions[0]);
                    Snackbar.make(v, R.string.submission_info_unsaved, Snackbar.LENGTH_SHORT).show();

                    submissions[0].saved = false;
                    v = null;

                } else {
                    new AccountManager(Authentication.reddit).save(submissions[0]);
                    Snackbar.make(v, R.string.submission_info_saved, Snackbar.LENGTH_SHORT).show();

                    submissions[0].saved = true;
                    v = null;


                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class AsyncLoadMore extends AsyncTask<MoreChildItem, Void, Integer> {

        public int position;

        public MoreCommentViewHolder holder;
        public int holderPos;

        public AsyncLoadMore(int position, int holderPos, MoreCommentViewHolder holder) {
            this.position = position;
            this.holderPos = holderPos;
            this.holder = holder;
        }

        @Override
        public void onPostExecute(Integer data) {


            listView.setItemAnimator(new ScaleInLeftAnimator());

            notifyItemRangeInserted(holderPos, data);

            currentPos = holderPos;
            toShiftTo = ((LinearLayoutManager) listView.getLayoutManager()).findLastVisibleItemPosition();
            shiftFrom = ((LinearLayoutManager) listView.getLayoutManager()).findFirstVisibleItemPosition();


        }

        @Override
        protected Integer doInBackground(MoreChildItem... params) {

            ArrayList<CommentObject> finalData = new ArrayList<>();
            int i = 0;

            if (params.length > 0) {
                try {
                   params[0].comment.loadMoreComments(Authentication.reddit);
                    for (CommentNode no :params[0].comment.walkTree()) {
                        if (!keys.containsKey(no.getComment().getFullName())) {
                            CommentObject obs = new CommentItem(no);

                            finalData.add(obs);

                            if (no.hasMoreComments()) {
                              finalData.add(new MoreChildItem(no, no.getMoreChildren()));
                            }
                            i++;
                        }
                    }
                } catch (Exception e) {
                    Log.w(LogUtil.getTag(), "Cannot load more comments " + e);
                }


                shifted += i;
                users.remove(position);
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemRemoved(holderPos);

                    }
                });
                users.addAll(position , finalData);

                for (int i2 = 0; i2 < users.size(); i2++) {
                    keys.put(users.get(i2).getName(), i2);
                }

            }
            return i;
        }
    }

    public class ReplyTaskComment extends AsyncTask<String, Void, String> {

        public Contribution sub;


        int finalPos;
        int finalPos1;
        CommentNode node;

        public ReplyTaskComment(Contribution n, int finalPos, int finalPos1, CommentNode node) {
            sub = n;
            this.finalPos = finalPos;
            this.finalPos1 = finalPos1;
            this.node = node;
        }

        public ReplyTaskComment(Contribution n) {
            sub = n;

        }

        @Override
        public void onPostExecute(final String s) {

            if (s != null) {


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dataSet.refreshLayout.setRefreshing(false);

                                dataSet.loadMore(CommentAdapter.this, submission.getSubredditName());
                                currentSelectedItem = s;
                            }
                        });


                    }
                }, 2000);


            }


        }

        @Override
        protected String doInBackground(String... comment) {
            if (Authentication.reddit.me() != null) {
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


}
