package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.CreateCardView;


public class SubmissionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter {

    private final RecyclerView listView;
    private final String subreddit;
    public Activity context;
    private final boolean custom;
    public SubredditPosts dataSet;
    public ArrayList<Submission> seen;
    private final int LOADING_SPINNER = 5;
    private final int SUBMISSION = 1;
    private final int NO_MORE = 3;
    private final int SPACER = 6;

    public SubmissionAdapter(Activity context, SubredditPosts dataSet, RecyclerView listView, String subreddit) {
        this.subreddit = subreddit.toLowerCase();
        this.listView = listView;
        this.dataSet = dataSet;
        this.context = context;
        this.seen = new ArrayList<>();
        custom = SettingValues.prefs.contains(Reddit.PREF_LAYOUT + subreddit.toLowerCase());
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
        if (position <= 0 && dataSet.posts.size() != 0) {
            return SPACER;
        } else if (dataSet.posts.size() != 0) {
            position -= (1);
        }
        if (position == dataSet.posts.size() && dataSet.posts.size() != 0 && !dataSet.offline && !dataSet.nomore) {
            return LOADING_SPINNER;
        } else if (position == dataSet.posts.size() && (dataSet.offline || dataSet.nomore)) {
            return NO_MORE;
        }
        return SUBMISSION;
    }

    int tag = 1;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        tag++;

        if (i == SPACER) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spacer, viewGroup, false);
            return new SpacerViewHolder(v);

        } else if (i == LOADING_SPINNER) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loadingmore, viewGroup, false);
            return new SubmissionFooterViewHolder(v);
        } else if (i == NO_MORE) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.nomoreposts, viewGroup, false);
            return new SubmissionFooterViewHolder(v);
        } else {
            View v = CreateCardView.CreateView(viewGroup, custom, subreddit);
            return new SubmissionViewHolder(v);
        }
    }

    int clicked;

    public void refreshView() {
        final RecyclerView.ItemAnimator a = listView.getItemAnimator();
        listView.setItemAnimator(null);
        notifyItemChanged(clicked);
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setItemAnimator(a);
            }
        }, 500);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder2, final int pos) {

        int i = pos != 0 ? pos - 1 : pos;


        if (holder2 instanceof SubmissionViewHolder) {
            final SubmissionViewHolder holder = (SubmissionViewHolder) holder2;

            final Submission submission = dataSet.posts.get(i);

            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), holder.itemView, subreddit, (subreddit.equals("frontpage") || (subreddit.equals("all")) || subreddit.contains(".") || subreddit.contains("+")));
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    if (Authentication.didOnline || submission.getComments() != null) {
                        holder.title.setAlpha(0.65f);
                        holder.leadImage.setAlpha(0.65f);
                        holder.thumbimage.setAlpha(0.65f);
                        if (SettingValues.tabletUI && holder2.itemView.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            Intent i2 = new Intent(holder2.itemView.getContext(), CommentsScreenPopup.class);
                            i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                            i2.putExtra(CommentsScreenPopup.EXTRA_PAGE, holder2.getAdapterPosition() - 1);
                            context.startActivityForResult(i2, 940);
                            clicked = holder2.getAdapterPosition();

                        } else {
                            Intent i2 = new Intent(holder2.itemView.getContext(), CommentsScreen.class);
                            i2.putExtra(CommentsScreen.EXTRA_PAGE, holder2.getAdapterPosition() - 1);
                            i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                            context.startActivityForResult(i2, 940);
                            clicked = holder2.getAdapterPosition();

                        }
                    } else {
                        Snackbar.make(holder.itemView, R.string.offline_comments_not_loaded, Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
            final boolean saved = submission.isSaved();

            new PopulateSubmissionViewHolder().populateSubmissionViewHolder(holder, submission, context, false, false, dataSet.posts, listView, custom, !dataSet.stillShow, dataSet.subreddit.toLowerCase());


        }
        if (holder2 instanceof SubmissionFooterViewHolder) {
            Handler handler = new Handler();

            final Runnable r = new Runnable() {
                public void run() {
                    notifyItemChanged(dataSet.posts.size() + 1); // the loading spinner to replaced by nomoreposts.xml
                }
            };

            handler.post(r);
        }
        if (holder2 instanceof SpacerViewHolder) {
            holder2.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(holder2.itemView.getWidth(), (context).findViewById(R.id.header).getHeight()));
            if (listView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (context).findViewById(R.id.header).getHeight());
                layoutParams.setFullSpan(true);
                holder2.itemView.setLayoutParams(layoutParams);
            }
        }
    }

    public class SubmissionFooterViewHolder extends RecyclerView.ViewHolder {
        public SubmissionFooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        if (dataSet.posts == null || dataSet.posts.size() == 0) {
            return 0;
        } else {
            return dataSet.posts.size() + 2; // Always account for footer
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
                if (ActionStates.isSaved(submissions[0])) {
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


    static void fixSliding(int position) {
        try {
            Reddit.lastposition.add(position, 0);
        } catch (IndexOutOfBoundsException e) {
            fixSliding(position - 1);
        }
    }
}