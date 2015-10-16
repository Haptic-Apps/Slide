package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Visuals.Pallete;


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

    public CommentAdapter(Context mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission) {

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

        if(currentSelectedItem != null && !currentSelectedItem.isEmpty()){
            int i = 1;
            for(CommentNode n : users){

                if(n.getComment().getFullName().contains(currentSelectedItem)){
                    RecyclerView.SmoothScroller smoothScroller = new CommentPage.TopSnappedSmoothScroller(listView.getContext(), (PreCachingLayoutManagerComments)listView.getLayoutManager());
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
        if(currentSelectedItem != null && !currentSelectedItem.isEmpty()){
            int i = 1;
            for(CommentNode n : users){

                if(n.getComment().getFullName().contains(currentSelectedItem)){
                    RecyclerView.SmoothScroller smoothScroller = new CommentPage.TopSnappedSmoothScroller(listView.getContext(), (PreCachingLayoutManagerComments)listView.getLayoutManager());
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
                if (saved) {
                    new AccountManager(Authentication.reddit).unsave(submissions[0]);
                    Snackbar.make(v, "Submission unsaved", Snackbar.LENGTH_SHORT).show();

                    saved = false;
                    v = null;

                } else {
                    new AccountManager(Authentication.reddit).save(submissions[0]);
                    Snackbar.make(v, "Submission saved", Snackbar.LENGTH_SHORT).show();

                    saved = true;
                    v = null;


                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
            return null;
        }
    }



    boolean saved;



    public CommentViewHolder currentlySelected;
    public String currentSelectedItem = "";

    public void doHighlighted(CommentViewHolder holder, Comment n) {
        if (currentlySelected != null) {
            doUnHighlighted(currentlySelected);
        }
        currentlySelected = holder;
        holder.dots.setVisibility(View.GONE);
        int color = Pallete.getColor(n.getSubredditName());
        currentSelectedItem = n.getFullName();
        holder.menu.setBackgroundColor(color);
        holder.replyArea.setBackgroundColor(color);

        holder.menu.setVisibility(View.VISIBLE);
        holder.replyArea.setVisibility(View.GONE);
        holder.itemView.findViewById(R.id.background).setBackgroundColor(Color.argb(50, Color.red(color), Color.green(color), Color.blue(color)));
    }

    public void doUnHighlighted(CommentViewHolder holder) {
        holder.menu.setVisibility(View.GONE);
        holder.replyArea.setVisibility(View.GONE);
        holder.dots.setVisibility(View.VISIBLE);

        TypedArray a = mContext.getTheme().obtainStyledAttributes(new ColorPreferences(mContext).getThemeSubreddit(submission.getSubredditName(), true).getBaseId(), new int[]{R.attr.card_background});
        int attributeResourceId = a.getResourceId(0, 0);
        holder.replyArea.setVisibility(View.GONE);

        holder.itemView.findViewById(R.id.background).setBackgroundColor(attributeResourceId);
    }

    public void doUnHighlighted(CommentViewHolder holder, Comment comment) {
        currentlySelected = null;
        currentSelectedItem = "";
        holder.menu.setVisibility(View.GONE);
        holder.replyArea.setVisibility(View.GONE);
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
        public void onPostExecute(Integer data){
            notifyItemRangeInserted(holderPos + 1 , holderPos + 1 + data);
        }
        @Override
        protected Integer doInBackground(CommentNode... params) {
            CommentNode n = params[0];
            n.loadFully(Authentication.reddit);
            ArrayList<CommentNode> finalData = new ArrayList<>();
            for (CommentNode no : n.walkTree()) {
                if(!no.getComment().getFullName().equals(n.getComment().getFullName())) {
                    Log.v("Slide", "ADDING " + no.getComment().getBody());
                    finalData.add(no);
                }

            }
            users.addAll(position , finalData);

            for (int i2 = 0; i2 < users.size(); i2++) {
                keys.put(users.get(i2).getComment().getFullName(), i2);
            }
            return finalData.size();
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHolder, int pos) {

        if (firstHolder instanceof CommentViewHolder) {
            final CommentViewHolder holder = (CommentViewHolder) firstHolder;
            int nextPos = pos - 1;

            nextPos = getRealPosition(nextPos);

            final CommentNode baseNode = users.get(nextPos);
            final Comment comment = baseNode.getComment();

            if (comment.getFullName().contains(currentSelectedItem) && !currentSelectedItem.isEmpty()) {
                doHighlighted(holder, comment);
            } else {
                doUnHighlighted(holder);
            }
            final int finalPos = nextPos;
            final int finalPos1 = pos;

          /*  if(baseNode.hasMoreComments() && !hasLoaded.contains(baseNode.getComment().getFullName())){
                holder.loadMore.setVisibility(View.VISIBLE);
                holder.loadMoreText.setText("Load " + baseNode.getMoreChildren().getCount() + " more replies");
                holder.loadMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hasLoaded.add(comment.getFullName());
                        holder.loadMore.setVisibility(View.GONE);
                        new AsyncLoadMore(finalPos + 1, finalPos1 ).execute(baseNode);

                    }
                });
            } else {
                holder.loadMore.setVisibility(View.GONE);

            }*/

            if(comment.getAuthor().toLowerCase().equals(Authentication.name.toLowerCase())){
                holder.itemView.findViewById(R.id.you).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.you).setVisibility(View.GONE);

            }
            if(comment.getAuthor().toLowerCase().equals(submission.getAuthor().toLowerCase())){
                holder.itemView.findViewById(R.id.op).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.op).setVisibility(View.GONE);

            }

            if (Authentication.isLoggedIn) {
                holder.reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.replyArea.setVisibility(View.VISIBLE);
                        holder.menu.setVisibility(View.GONE);
                    }
                });
                holder.send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.replyArea.setVisibility(View.GONE);
                        holder.menu.setVisibility(View.VISIBLE);
                        dataSet.refreshLayout.setRefreshing(true);
                        new ReplyTaskComment(comment, finalPos, finalPos1, baseNode).execute(((EditText) firstHolder.itemView.findViewById(R.id.replyLine)).getText().toString());


                    }
                });
                holder.discard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.menu.setVisibility(View.VISIBLE);

                        holder.replyArea.setVisibility(View.GONE);
                    }
                });

            } else {
                holder.reply.setVisibility(View.GONE);
                holder.upvote.setVisibility(View.GONE);
                holder.downvote.setVisibility(View.GONE);

            }

            firstHolder.itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.commentmenu, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setText(comment.getBody());

                    ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + comment.getAuthor());
                    dialoglayout.findViewById(R.id.userpopup).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, Profile.class);
                            i.putExtra("profile", comment.getAuthor());
                            mContext.startActivity(i);
                        }
                    });


                    dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String urlString = submission.getUrl() +comment.getFullName().substring(3, comment.getFullName().length()) + "?context=3";

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
                            String urlString = "http://reddit.com" + submission.getPermalink() +comment.getFullName().substring(3, comment.getFullName().length()) + "?context=3";

                            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(mContext.CLIPBOARD_SERVICE);
                            clipboard.setText(urlString);
                            Toast.makeText(mContext, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
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

            holder.author.setText(comment.getAuthor());
            if (comment.getAuthorFlair() != null && comment.getAuthorFlair().getText() != null && !comment.getAuthorFlair().getText().isEmpty()) {
                holder.itemView.findViewById(R.id.flairbubble).setVisibility(View.VISIBLE);
                ((TextView) holder.itemView.findViewById(R.id.text)).setText(comment.getAuthorFlair().getText());

            } else {
                holder.itemView.findViewById(R.id.flairbubble).setVisibility(View.GONE);

            }
            holder.content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (currentSelectedItem.contains(comment.getFullName())) {
                        doUnHighlighted(holder, comment);
                    } else {
                        doHighlighted(holder, comment);
                    }
                    return true;
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (currentSelectedItem.contains(comment.getFullName())) {
                        doUnHighlighted(holder, comment);
                    } else {
                        doHighlighted(holder, comment);
                    }
                    return true;
                }
            });
            saved = submission.isSaved();
            if(comment.isScoreHidden()){
                holder.score.setText("[SCORE HIDDEN]");

            } else {
                holder.score.setText(comment.getScore() + "");

            }
            if (baseNode.isTopLevel()) {
                holder.itemView.findViewById(R.id.next).setVisibility(View.VISIBLE);
            } else {
                holder.itemView.findViewById(R.id.next).setVisibility(View.GONE);

            }
            holder.time.setText(TimeUtils.getTimeAgo(comment.getCreatedUtc().getTime()));

            new MakeTextviewClickable().ParseTextWithLinksTextViewComment(comment.getDataNode().get("body_html").asText(), holder.content, (Activity) mContext, submission.getSubredditName());
            if (comment.getTimesGilded() > 0) {
                holder.gild.setVisibility(View.VISIBLE);
                ((TextView) holder.gild.findViewById(R.id.gildtext)).setText("" + comment.getTimesGilded());
            } else {
                holder.gild.setVisibility(View.GONE);
            }

            if (hiddenPersons.contains(comment.getFullName())) {
                holder.children.setVisibility(View.VISIBLE);
                ((TextView) holder.children.findViewById(R.id.flairtext)).setText("+" + getChildNumber(baseNode));
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
                    if (currentSelectedItem.contains(comment.getFullName())) {
                        doUnHighlighted(holder, comment);
                    } else {

                        doOnClick(holder, baseNode, comment);
                    }
                }
            });
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentSelectedItem.contains(comment.getFullName())) {
                        doUnHighlighted(holder, comment);
                    } else {

                        doOnClick(holder, baseNode, comment);
                    }
                }
            });

            holder.dots.removeAllViews();
            for (int i = 0; i < baseNode.getDepth() - 1; i++) {
                ((Activity) mContext).getLayoutInflater().inflate(R.layout.dot, holder.dots);

            }
            holder.itemView.findViewById(R.id.dot).setVisibility(View.VISIBLE);

            if (baseNode.getDepth() - 1 > 0) {
                View v = holder.itemView.findViewById(R.id.dot);
                int i22 = baseNode.getDepth() - 2;
                if (i22 % 5 == 0) {
                    v.setBackgroundColor(Color.parseColor("#2196F3")); //blue
                } else if (i22 % 4 == 0) {
                    v.setBackgroundColor(Color.parseColor("#4CAF50")); //green

                } else if (i22 % 3 == 0) {
                    v.setBackgroundColor(Color.parseColor("#FFC107")); //yellow

                } else if (i22 % 2 == 0) {
                    v.setBackgroundColor(Color.parseColor("#FF9800")); //orange

                } else {
                    v.setBackgroundColor(Color.parseColor("#F44336")); //red
                }
            } else {
                holder.itemView.findViewById(R.id.dot).setVisibility(View.GONE);
            }
        } else {
            new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder((SubmissionViewHolder) firstHolder, submission, mContext, true);

            if (Authentication.isLoggedIn) {
                firstHolder.itemView.findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.VISIBLE);
                        firstHolder.itemView.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dataSet.refreshLayout.setRefreshing(true);

                                new ReplyTaskComment(submission).execute(((EditText) firstHolder.itemView.findViewById(R.id.textinner)).getText().toString());
                                firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);
                            }
                        });

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
                            if (saved) {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText("Save post");
                            } else {
                                ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText("Post saved");

                            }
                            new AsyncSave(firstHolder.itemView).execute(submission);

                        }
                    });
                    if (saved) {
                        ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText("Post saved");
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
                            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(mContext.CLIPBOARD_SERVICE);
                            clipboard.setText("http://reddit.com" + submission.getPermalink());
                            Toast.makeText(mContext, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
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
            isHolder.menu.setVisibility(View.VISIBLE);
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

            if(s != null) {


                    try {
                        dataSet.loadMore(CommentAdapter.this, true, submission.getSubredditName());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


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
            if(!ignored.getComment().getFullName().equals(n.getComment().getFullName())) {
                String name = ignored.getComment().getFullName();
                if (hiddenPersons.contains(name)) {
                    hiddenPersons.remove(name);
                }
                if(hidden.contains(name)) {
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
            if(!ignored.getComment().getFullName().equals(n.getComment().getFullName())) {

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