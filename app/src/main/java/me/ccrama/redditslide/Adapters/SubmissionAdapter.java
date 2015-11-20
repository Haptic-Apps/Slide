package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.Pallete;


public class SubmissionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter {

    public static Activity mContext;
    private final RecyclerView listView;
    private final String subreddit;
    private final boolean custom;
    public SubredditPosts dataSet;
    public ArrayList<Submission> seen;
    public ArrayList<RecyclerView.ViewHolder> views;

    public SubmissionAdapter(Activity mContext, SubredditPosts dataSet, RecyclerView listView, String subreddit) {
        this.views = new ArrayList<>();
        this.mContext = mContext;
        this.subreddit = subreddit.toLowerCase();
        this.listView = listView;
        this.dataSet = dataSet;
        this.seen = new ArrayList<>();
        custom = SettingValues.prefs.contains("PRESET" + subreddit.toLowerCase());

        Log.v("Slide", subreddit + " CUSTOM IS " + custom);
    }

    @Override
    public void setError(Boolean b) {
        listView.setAdapter(new ErrorAdapter());
        Log.v("Slide", "SETTING ADAPTER");
    }

    @Override
    public void undoSetError() {
        listView.setAdapter(this);
    }

    @Override
    public int getItemViewType(int position) {

        if (position == dataSet.posts.size()  &&dataSet.posts.size() != 0 &&!dataSet.offline) {

            return 5;
        }
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (i == 5) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loadingmore, viewGroup, false);
            return new ContributionAdapter.EmptyViewHolder(v);
        } else {
            View v = CreateCardView.CreateView(viewGroup, custom, subreddit);
            return new SubmissionViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder2, final int i) {
        if (holder2 instanceof SubmissionViewHolder) {
            final SubmissionViewHolder holder = (SubmissionViewHolder) holder2;
            final Submission submission = dataSet.posts.get(i);

            CreateCardView.resetColorCard(holder.itemView);
            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), holder.itemView, subreddit, custom);
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (!dataSet.offline) {
                        DataShare.sharedSubreddit = dataSet.posts;
                        holder2.itemView.setAlpha(0.5f);
                        if (Reddit.tabletUI && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            Intent i2 = new Intent(mContext, CommentsScreenPopup.class);
                            i2.putExtra("page", holder2.getAdapterPosition());
                            (mContext).startActivity(i2);

                        } else {
                            Intent i2 = new Intent(mContext, CommentsScreen.class);
                            i2.putExtra("page", holder2.getAdapterPosition());
                            (mContext).startActivity(i2);
                        }
                    } else {
                        Snackbar.make(holder.itemView, "Please go online and refresh the subreddit to do that", Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
            final boolean saved = submission.isSaved();


            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (dataSet.offline) {

                        Snackbar.make(holder.itemView, "Please go online and refresh the subreddit to do that", Snackbar.LENGTH_SHORT).show();

                    } else {
                        LayoutInflater inflater = mContext.getLayoutInflater();
                        final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);
                        final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                        title.setText(submission.getTitle());

                        ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                        ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                        dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent i = new Intent(mContext, Profile.class);
                                i.putExtra("profile", submission.getAuthor());
                                mContext.startActivity(i);
                            }
                        });

                        dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
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
                                    ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_save);
                                } else {
                                    ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);

                                }
                                new AsyncSave(holder.itemView).execute(submission);

                            }
                        });
                        if (saved) {
                            ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText(R.string.submission_post_saved);
                        }
                        dialoglayout.findViewById(R.id.gild).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String urlString = "https://reddit.com" + submission.getPermalink();
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
                                new AlertDialogWrapper.Builder(mContext).setTitle(R.string.submission_share_title)
                                        .setNegativeButton(R.string.submission_share_reddit, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), mContext);

                                            }
                                        }).setPositiveButton(R.string.submission_share_content, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Reddit.defaultShareText(submission.getUrl(), mContext);

                                    }
                                }).show();

                            }
                        });
                        if (!Authentication.isLoggedIn) {
                            dialoglayout.findViewById(R.id.save).setVisibility(View.GONE);
                            dialoglayout.findViewById(R.id.gild).setVisibility(View.GONE);

                        }
                        title.setBackgroundColor(Pallete.getColor(submission.getSubredditName()));

                        builder.setView(dialoglayout);
                        final Dialog d = builder.show();
                        dialoglayout.findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final int pos = dataSet.posts.indexOf(submission);
                                final Submission old = dataSet.posts.get(pos);
                                dataSet.posts.remove(submission);
                                notifyItemRemoved(pos);
                                d.dismiss();
                                Hidden.setHidden(old);

                                Snackbar.make(listView, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dataSet.posts.add(pos, old);
                                        notifyItemInserted(pos);
                                        Hidden.undoHidden(old);

                                    }
                                }).show();

                            }
                        });
                    }
                    return true;
                }

            });

            new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder(holder, submission, mContext, false, false, dataSet.posts, listView, custom, dataSet.offline);

            holder.itemView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onLayoutChange(final View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    setAnimation(v, i);

                }
            });
        }
        views.add(holder2);
    }

    @Override
    public int getItemCount() {
        if (dataSet.posts == null || dataSet.posts.size() == 0) {
            return 0;
        } else if (dataSet.nomore || dataSet.offline ) {

            return dataSet.posts.size();
        } else {
            return dataSet.posts.size() + 1;

        }
    }

    public static class AsyncSave extends AsyncTask<Submission, Void, Void> {

        View v;

        public AsyncSave(View v) {
            this.v = v;
        }


        @Override
        protected Void doInBackground(Submission... submissions) {
            try {
                if (submissions[0].isSaved()) {
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
            } catch (Exception e) {
                return null;
            }
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setAnimation(View viewToAnimate, int position) {
        try {
            if (position >= Reddit.lastposition.get(Reddit.currentPosition) - 1) {
                int cx = viewToAnimate.getWidth() / 2;
                int cy = viewToAnimate.getHeight() / 2;
                int finalRadius = Math.max(viewToAnimate.getWidth(), viewToAnimate.getHeight());

                final Animator anim =
                        ViewAnimationUtils.createCircularReveal(viewToAnimate, cx, cy, 0, finalRadius);
                anim.setDuration(Reddit.enter_animation_time);
                anim.setInterpolator(new FastOutSlowInInterpolator());
                viewToAnimate.setVisibility(View.VISIBLE);
                anim.start();
                Reddit.lastposition.set(Reddit.currentPosition, Reddit.lastposition.get(Reddit.currentPosition) + 1);
            }
        } catch (IndexOutOfBoundsException e) {
            fixSliding(Reddit.currentPosition);
        }
    }

    static void fixSliding(int position) {
        try {
            System.out.println("Fixing..");
            Reddit.lastposition.add(position, 0);
        } catch (IndexOutOfBoundsException e) {
            fixSliding(position - 1);
        }
    }


}