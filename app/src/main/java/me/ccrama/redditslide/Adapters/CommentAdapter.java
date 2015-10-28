package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.HashMap;

import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Vote;


public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public Context mContext;
    public SubmissionComments dataSet;
    RecyclerView listView;
    ArrayList<String> up;
    ArrayList<String> down;


    static int HEADER = 1;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (i == HEADER) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_fullscreen, viewGroup, false);
            return new SubmissionViewHolder(v);
        } else {

            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment, viewGroup, false);
            return new CommentViewHolder(v);

        }

    }

    public Submission submission;

    public CommentAdapter(Context mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission, FragmentManager fm) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;
        this.fm = fm;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getComment().getFullName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        replie = new ArrayList<>();
        up = new ArrayList<>();
        down = new ArrayList<>();


        isSame = false;

    }


    public int currentlyHighlighted;

    public void reset(Context mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getComment().getFullName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        replie = new ArrayList<>();


        isSame = false;
        notifyItemRangeInserted(1, users.size() + 1);

        if (currentSelectedItem != null && !currentSelectedItem.isEmpty()) {
            int i = 1;
            for (CommentNode n : users) {

                if (n.getComment().getFullName().contains(currentSelectedItem)) {
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
                keys.put(users.get(i).getComment().getFullName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        replie = new ArrayList<>();


        isSame = false;
        notifyDataSetChanged();
        if (currentSelectedItem != null && !currentSelectedItem.isEmpty()) {
            int i = 1;
            for (CommentNode n : users) {

                if (n.getComment().getFullName().contains(currentSelectedItem)) {
                    RecyclerView.SmoothScroller smoothScroller = new CommentPage.TopSnappedSmoothScroller(listView.getContext(), (PreCachingLayoutManagerComments) listView.getLayoutManager());
                    smoothScroller.setTargetPosition(i);
                    (listView.getLayoutManager()).startSmoothScroll(smoothScroller);
                    break;
                }
                i++;
            }
        }
    }

    boolean isSame;

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


    public CommentViewHolder currentlySelected;
    public String currentSelectedItem = "";

    public void doHighlighted(final CommentViewHolder holder, final Comment n, final CommentNode baseNode, final int finalPos, final int finalPos1) {
        if (currentlySelected != null) {
            doUnHighlighted(currentlySelected);
        }
        currentlySelected = holder;
        holder.dots.setVisibility(View.GONE);
        int color = Pallete.getColor(n.getSubredditName());
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
        if (Authentication.isLoggedIn) {
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
                title.setText(n.getBody());

                ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + n.getAuthor());
                dialoglayout.findViewById(R.id.userpopup).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra("profile", n.getAuthor());
                        mContext.startActivity(i);
                    }
                });


                dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String urlString = submission.getUrl() + n.getFullName().substring(3, n.getFullName().length()) + "?context=3";

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage("com.android.chrome"); //Force open in chrome so it doesn't open back in Slide
                        try {
                            mContext.startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            intent.setPackage(null);
                            mContext.startActivity(intent);
                        }
                    }
                });
                dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String urlString = "http://reddit.com" + submission.getPermalink() + n.getFullName().substring(3, n.getFullName().length()) + "?context=3";

                        Reddit.defaultShareText(urlString, mContext);
                    }
                });
                if (!Authentication.isLoggedIn) {

                    dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                }
                title.setBackgroundColor(Pallete.getColor(submission.getSubredditName()));

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
                    upvote.clearColorFilter();

                } else if (down.contains(n.getFullName())) {
                    new Vote(true, v, mContext).execute(n);
                    up.add(n.getFullName());

                    down.remove(n.getFullName());
                    holder.score.setTextColor(holder.textColorUp);
                    upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
                } else {
                    new Vote(true, v, mContext).execute(n);

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
                    downvote.clearColorFilter();

                } else if (up.contains(n.getFullName())) {
                    new Vote(false, v, mContext).execute(n);
                    down.add(n.getFullName());
                    up.remove(n.getFullName());
                    holder.score.setTextColor(holder.textColorDown);
                    downvote.setColorFilter(holder.textColorDown);

                } else {
                    new Vote(false, v, mContext).execute(n);

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
        TypedArray a = mContext.getTheme().obtainStyledAttributes(new ColorPreferences(mContext).getThemeSubreddit(submission.getSubredditName(), true).getBaseId(), new int[]{R.attr.card_background});
        int attributeResourceId = a.getResourceId(0, 0);

        holder.itemView.findViewById(R.id.background).setBackgroundColor(attributeResourceId);
    }

    public void doUnHighlighted(CommentViewHolder holder, Comment comment) {
        currentlySelected = null;
        currentSelectedItem = "";
       holder.menuArea.removeAllViews();
        holder.dots.setVisibility(View.VISIBLE);

        TypedArray a = mContext.getTheme().obtainStyledAttributes(new ColorPreferences(mContext).getThemeSubreddit(submission.getSubredditName(), true).getBaseId(), new int[]{R.attr.card_background});
        int attributeResourceId = a.getResourceId(0, 0);
        holder.itemView.findViewById(R.id.background).setBackgroundColor(attributeResourceId);
    }

    public ArrayList<String> hasLoaded = new ArrayList<>();

    public class AsyncLoadMore extends AsyncTask<CommentNode, Void, Integer> {

        public int position;

        public int holderPos;

        public AsyncLoadMore(int position, int holderPos) {
            this.position = position;
            this.holderPos = holderPos;
        }

        @Override
        public void onPostExecute(Integer data) {
            notifyItemRangeInserted(holderPos + 1, holderPos + 1 + data);
        }

        @Override
        protected Integer doInBackground(CommentNode... params) {
            CommentNode n = params[0];
            n.loadFully(Authentication.reddit);
            ArrayList<CommentNode> finalData = new ArrayList<>();
            for (CommentNode no : n.walkTree()) {
                if (!no.getComment().getFullName().equals(n.getComment().getFullName())) {
                    finalData.add(no);
                }

            }
            users.addAll(position, finalData);

            for (int i2 = 0; i2 < users.size(); i2++) {
                keys.put(users.get(i2).getComment().getFullName(), i2);
            }
            return finalData.size();
        }
    }

    public void doLongClick(CommentViewHolder holder, Comment comment, CommentNode baseNode, int finalPos, int finalPos1) {
        if (currentSelectedItem.contains(comment.getFullName())) {
            doUnHighlighted(holder, comment);
        } else {
            doHighlighted(holder, comment, baseNode, finalPos, finalPos1);
        }
    }

    public FragmentManager fm;

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

            final CommentNode baseNode = users.get(nextPos);
            final Comment comment = baseNode.getComment();

            if (comment.getFullName().contains(currentSelectedItem) && !currentSelectedItem.isEmpty()) {
                doHighlighted(holder, comment, baseNode, finalPos, finalPos1);
            } else {
                doUnHighlighted(holder);
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

            if (up.contains(comment.getFullName())) {
                holder.score.setTextColor(holder.textColorUp);
            } else if (down.contains(comment.getFullName())) {
                holder.score.setTextColor(holder.textColorDown);
            } else {
                holder.score.setTextColor(holder.textColorRegular);
            }

            
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



            holder.author.setText(comment.getAuthor());
            if (comment.getAuthorFlair() != null && comment.getAuthorFlair().getText() != null && !comment.getAuthorFlair().getText().isEmpty()) {
                holder.flairBubble.setVisibility(View.VISIBLE);
                holder.flairText.setText(comment.getAuthorFlair().getText());

            } else {

                if (holder.flairBubble.getVisibility() == View.VISIBLE)
                    holder.flairBubble.setVisibility(View.GONE);

            }
            holder.content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (Reddit.swap) {
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
                    if (Reddit.swap) {
                        doOnClick(holder, comment, baseNode);

                    } else {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    }
                    return true;
                }
            });
            if (comment.isScoreHidden()) {
                String scoreText = mContext.getString(R.string.misc_score_hidden).toUpperCase();;
                holder.score.setText("[" + scoreText +"]");

            } else {
                holder.score.setText(comment.getScore() + "");

            }
            if (baseNode.isTopLevel()) {
                holder.itemView.findViewById(R.id.next).setVisibility(View.VISIBLE);
            } else {
                if (holder.itemView.findViewById(R.id.next).getVisibility() == View.VISIBLE)

                    holder.itemView.findViewById(R.id.next).setVisibility(View.GONE);

            }
            holder.time.setText(TimeUtils.getTimeAgo(comment.getCreatedUtc().getTime(), mContext));

            if (comment.getTimesGilded() > 0) {
                holder.gild.setVisibility(View.VISIBLE);
                ((TextView) holder.gild.findViewById(R.id.gildtext)).setText("" + comment.getTimesGilded());
            } else {

                if (holder.gild.getVisibility() == View.VISIBLE)

                    holder.gild.setVisibility(View.GONE);
            }

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

                    Intent i2 = new Intent(mContext, Profile.class);
                    i2.putExtra("profile", comment.getAuthor());
                    mContext.startActivity(i2);
                }
            });
            holder.author.setTextColor(Pallete.getFontColorUser(comment.getAuthor()));
            if (holder.author.getCurrentTextColor() == 0) {
                holder.author.setTextColor(holder.time.getCurrentTextColor());
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Reddit.swap) {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    } else {
                        doOnClick(holder, comment, baseNode);
                    }
                }
            });
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Reddit.swap) {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    } else {
                        doOnClick(holder, comment, baseNode);
                    }
                }
            });

            holder.dot.setVisibility(View.VISIBLE);



               int dwidth =  (int)(3 *  Resources.getSystem().getDisplayMetrics().density);
                    int width = 0;
            for (int i = 0; i < baseNode.getDepth() - 1; i++) {
                width += dwidth;
            }
            holder.dots.setPadding(width, 0, 0, 0);

            if (baseNode.getDepth() - 1 > 0) {
                int i22 = baseNode.getDepth() - 2;
                if (i22 % 5 == 0) {
                    holder.dot.setBackgroundColor(Color.parseColor("#2196F3")); //blue
                } else if (i22 % 4 == 0) {
                    holder.dot.setBackgroundColor(Color.parseColor("#4CAF50")); //green

                } else if (i22 % 3 == 0) {
                    holder.dot.setBackgroundColor(Color.parseColor("#FFC107")); //yellow

                } else if (i22 % 2 == 0) {
                    holder.dot.setBackgroundColor(Color.parseColor("#FF9800")); //orange

                } else {
                    holder.dot.setBackgroundColor(Color.parseColor("#F44336")); //red
                }
            } else {
                holder.dot.setVisibility(View.GONE);
            }
        } else {
            new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder((SubmissionViewHolder) firstHolder, submission, mContext, true, true, null, null, false);
            if (Authentication.isLoggedIn) {
                firstHolder.itemView.findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.VISIBLE);
                        DoEditorActions.doActions(((EditText) firstHolder.itemView.findViewById(R.id.replyLine)), firstHolder.itemView, fm);

                        firstHolder.itemView.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dataSet.refreshLayout.setRefreshing(true);

                                new ReplyTaskComment(submission).execute(((EditText) firstHolder.itemView.findViewById(R.id.replyLine)).getText().toString());
                                firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);
                            }
                        });

                    }
                });
                firstHolder.itemView.findViewById(R.id.discard).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);
                    }
                });
            } else {
                firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);

            }
            firstHolder.itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setText(submission.getTitle());

                    ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                    ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                    dialoglayout.findViewById(R.id.userpopup).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, Profile.class);
                            i.putExtra("profile", submission.getAuthor());
                            mContext.startActivity(i);
                        }
                    });
                    dialoglayout.findViewById(R.id.hide).setVisibility(View.GONE);
                    dialoglayout.findViewById(R.id.subpopup).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, SubredditView.class);
                            i.putExtra("subreddit", submission.getSubredditName());
                            mContext.startActivity(i);
                        }
                    });

                    dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (submission.saved) {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_save_post);
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
                            String urlString = submission.getUrl();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setPackage("com.android.chrome"); //Force open in chrome so it doesn't open back in Slide
                            try {
                                mContext.startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                intent.setPackage(null);
                                mContext.startActivity(intent);
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Reddit.defaultShareText("http://reddit.com" + submission.getPermalink(), mContext);

                        }
                    });
                    if (!Authentication.isLoggedIn) {
                        dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);
                        dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                    }
                    title.setBackgroundColor(Pallete.getColor(submission.getSubredditName()));

                    builder.setView(dialoglayout);
                    builder.show();
                }
            });
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
                //todo maybe         holder.content.setVisibility(View.VISIBLE);

            } else {
                hideAll(baseNode, holder.getAdapterPosition() + 1
                );
                //todo maybe   holder.content.setVisibility(View.GONE);

                hiddenPersons.add(comment.getFullName());
            }
            clickpos = holder.getAdapterPosition() + 1;
            if (hiddenPersons.contains(comment.getFullName())) {
                holder.children.setVisibility(View.VISIBLE);
                ((TextView) holder.children.findViewById(R.id.flairtext)).setText("+" + getChildNumber(baseNode));
            } else {
                holder.children.setVisibility(View.GONE);

            }
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
        public void onPostExecute(String s) {
            dataSet.refreshLayout.setRefreshing(false);

            if (s != null) {


                dataSet.loadMore(CommentAdapter.this, submission.getSubredditName());


                currentSelectedItem = s;

            }


        }

        @Override
        protected String doInBackground(String... comment) {
            if (Authentication.reddit.me() != null) {
                try {
                    return new AccountManager(Authentication.reddit).reply(sub, comment[0]);


                } catch (ApiException e) {
                    Log.v("Slide", "UH OH!!");
                    //todo this
                }


            }

            return null;


        }
    }

    public int clickpos;

    public CommentViewHolder isHolder;

    public boolean isClicking;

    private int getChildNumber(CommentNode user) {
        int i = 0;
        for (CommentNode n : user.walkTree()) {
            i++;
        }
        return i - 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEADER;

        return 2;
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
        notifyItemRangeInserted(i, counter);


    }

    public void hideAll(CommentNode n, int i) {
        int counter = hideNumber(n, 0);
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
                i += unhideNumber(ignored, 0);
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
                i += hideNumber(ignored, 0);
            }
        }
        return i;
    }

    public HashMap<String, Integer> keys = new HashMap<>();
    ArrayList<CommentNode> users;
    ArrayList<String> hidden;
    ArrayList<String> hiddenPersons;

    private int getRealPosition(int position) {

        int hElements = getHiddenCountUpTo(position);
        int diff = 0;
        for (int i = 0; i < hElements; i++) {
            diff++;
            if (hidden.contains(users.get(position + diff).getComment().getFullName())) {
                i--;
            }
        }
        return (position + diff);
    }

    private int getHiddenCountUpTo(int location) {
        int count = 0;
        for (int i = 0; i <= location; i++) {
            if (hidden.contains(users.get(i).getComment().getFullName()))
                count++;
        }
        return count;
    }

    ArrayList<String> replie;


}