package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.util.LogUtil;

;


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
    SubmissionDisplay displayer;

    public SubmissionAdapter(Activity context, SubredditPosts dataSet, RecyclerView listView, String subreddit, SubmissionDisplay displayer) {
        this.subreddit = subreddit.toLowerCase();
        this.listView = listView;
        this.dataSet = dataSet;
        this.context = context;
        this.seen = new ArrayList<>();
        custom = SettingValues.prefs.contains(Reddit.PREF_LAYOUT + subreddit.toLowerCase());
        this.displayer = displayer;
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
    public void refreshView(boolean ignore18) {
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
    public void refreshView(ArrayList<Integer> seen) {
        listView.setItemAnimator(null);
        final RecyclerView.ItemAnimator a = listView.getItemAnimator();

        for (int i : seen) {
            LogUtil.v("Changed " + (i + 1));
            notifyItemChanged(i + 1);
        }
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

            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), holder.itemView, subreddit, (subreddit.equals("frontpage") || subreddit.equals("mod") || subreddit.equals("friends") || (subreddit.equals("all")) || subreddit.contains(".") || subreddit.contains("+")));
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                                                   @Override
                                                   public void onClick(View arg0) {

                                                       if (Authentication.didOnline || submission.getComments() != null) {
                                                           holder.title.setAlpha(0.54f);

                                                           if (context instanceof MainActivity) {
                                                               final MainActivity a = (MainActivity) context;
                                                               if (a.singleMode && a.commentPager && a.adapter instanceof MainActivity.OverviewPagerAdapterComment) {

                                                                   if (a.openingComments != submission) {
                                                                       clicked = holder2.getAdapterPosition();
                                                                       a.openingComments = submission;
                                                                       a.toOpenComments = a.pager.getCurrentItem() + 1;
                                                                       a.currentComment = holder.getAdapterPosition() - 1;
                                                                       ContentType.ImageType type = ContentType.getImageType(submission);
                                                                       if (submission.isNsfw() && !SettingValues.storeNSFWHistory) {
                                                                           //Do nothing if the post is NSFW and storeNSFWHistory is not enabled
                                                                       } else {
                                                                           HasSeen.addSeen(submission.getFullName());
                                                                           LastComments.setComments(submission);
                                                                       }
                                                                       ((MainActivity.OverviewPagerAdapterComment) (a).adapter).storedFragment = (a).adapter.getCurrentFragment();
                                                                       ((MainActivity.OverviewPagerAdapterComment) (a).adapter).size = a.toOpenComments + 1;
                                                                       a.adapter.notifyDataSetChanged();
                                                                   }
                                                                   a.pager.postDelayed(new Runnable() {
                                                                       @Override
                                                                       public void run() {
                                                                           a.pager.setCurrentItem(a.pager.getCurrentItem() + 1, true);
                                                                       }
                                                                   }, 400);

                                                               } else {
                                                                   Intent i2 = new Intent(context, CommentsScreen.class);
                                                                   i2.putExtra(CommentsScreen.EXTRA_PAGE, holder2.getAdapterPosition() - 1);
                                                                   i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                                                                   context.startActivityForResult(i2, 940);
                                                                   clicked = holder2.getAdapterPosition();
                                                               }
                                                           } else if (context instanceof SubredditView) {
                                                               final SubredditView a = (SubredditView) context;
                                                               if (a.singleMode && a.commentPager) {

                                                                   if (a.openingComments != submission) {
                                                                       clicked = holder2.getAdapterPosition();
                                                                       a.openingComments = submission;
                                                                       a.currentComment = holder.getAdapterPosition() - 1;
                                                                       ContentType.ImageType type = ContentType.getImageType(submission);
                                                                       if (submission.isNsfw() && !SettingValues.storeNSFWHistory) {
                                                                           //Do nothing if the post is NSFW and storeNSFWHistory is not enabled
                                                                       } else {
                                                                           HasSeen.addSeen(submission.getFullName());
                                                                           LastComments.setComments(submission);
                                                                       }
                                                                       ((SubredditView.OverviewPagerAdapterComment) (a).adapter).storedFragment = (a).adapter.getCurrentFragment();
                                                                       ((SubredditView.OverviewPagerAdapterComment) a.adapter).size = 3;
                                                                       a.adapter.notifyDataSetChanged();
                                                                   }
                                                                   a.pager.postDelayed(new Runnable() {
                                                                       @Override
                                                                       public void run() {
                                                                           a.pager.setCurrentItem(a.pager.getCurrentItem() + 1, true);
                                                                       }
                                                                   }, 400);

                                                               } else {
                                                                   Intent i2 = new Intent(context, CommentsScreen.class);
                                                                   i2.putExtra(CommentsScreen.EXTRA_PAGE, holder2.getAdapterPosition() - 1);
                                                                   i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                                                                   context.startActivityForResult(i2, 940);
                                                                   clicked = holder2.getAdapterPosition();
                                                               }
                                                           }
                                                       } else {
                                                           Snackbar s = Snackbar.make(holder.itemView, R.string.offline_comments_not_loaded, Snackbar.LENGTH_SHORT);
                                                           View view = s.getView();
                                                           TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                           tv.setTextColor(Color.WHITE);
                                                           s.show();

                                                       }

                                                   }
                                               }

            );
            final boolean saved = submission.isSaved();

            new PopulateSubmissionViewHolder().populateSubmissionViewHolder(holder, submission, context, false, false, dataSet.posts, listView, custom, !dataSet.stillShow, dataSet.subreddit.toLowerCase()

            );


        }
        if (holder2 instanceof SubmissionFooterViewHolder) {
            Handler handler = new Handler();

            final Runnable r = new Runnable() {
                public void run() {
                    notifyItemChanged(dataSet.posts.size() + 1); // the loading spinner to replaced by nomoreposts.xml
                }
            };

            handler.post(r);

            if (holder2.itemView.findViewById(R.id.reload) != null) {
                holder2.itemView.findViewById(R.id.reload).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataSet.loadMore(context, displayer, true);
                    }
                });

            }
        }
        if (holder2 instanceof SpacerViewHolder) {
            View header = (context).findViewById(R.id.header);
            int height = header.getHeight();
            if (height == 0) {
                header.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                height = header.getMeasuredHeight();
                holder2.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(holder2.itemView.getWidth(), height));
                if (listView.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
                    CatchStaggeredGridLayoutManager.LayoutParams layoutParams = new CatchStaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                    layoutParams.setFullSpan(true);
                    holder2.itemView.setLayoutParams(layoutParams);
                }
            } else {
                holder2.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(holder2.itemView.getWidth(), height));
                if (listView.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
                    CatchStaggeredGridLayoutManager.LayoutParams layoutParams = new CatchStaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                    layoutParams.setFullSpan(true);
                    holder2.itemView.setLayoutParams(layoutParams);
                }
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
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View view = s.getView();
                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();
                        }
                    });


                    submissions[0].saved = false;
                    v = null;
                } else {
                    new AccountManager(Authentication.reddit).save(submissions[0]);
                    final Snackbar s = Snackbar.make(v, R.string.submission_info_saved, Snackbar.LENGTH_SHORT);
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View view = s.getView();
                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
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


    static void fixSliding(int position) {
        try {
            Reddit.lastposition.add(position, 0);
        } catch (IndexOutOfBoundsException e) {
            fixSliding(position - 1);
        }
    }
}