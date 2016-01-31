package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.Palette;


public class SubmissionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter {

    public static Activity sContext;
    private final RecyclerView listView;
    private final String subreddit;
    private final boolean custom;
    public SubredditPosts dataSet;
    public ArrayList<Submission> seen;
    public ArrayList<RecyclerView.ViewHolder> views;
    private final int LOADING_SPINNER = 5;
    private final int SUBMISSION = 1;
    private final int NO_MORE = 3;

    public SubmissionAdapter(Activity mContext, SubredditPosts dataSet, RecyclerView listView, String subreddit) {
        this.views = new ArrayList<>();
        sContext = mContext;
        this.subreddit = subreddit.toLowerCase();
        this.listView = listView;
        this.dataSet = dataSet;
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
        if (position == dataSet.posts.size() && dataSet.posts.size() != 0 && !dataSet.offline && !dataSet.nomore) {
            return LOADING_SPINNER;
        } else if (position == dataSet.posts.size() && (dataSet.offline || dataSet.nomore)) {
            return NO_MORE;
        }
        return SUBMISSION;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == LOADING_SPINNER) {
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

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder2, final int i) {
        if (holder2 instanceof SubmissionViewHolder) {
            final SubmissionViewHolder holder = (SubmissionViewHolder) holder2;
            final Submission submission = dataSet.posts.get(i);

            CreateCardView.resetColorCard(holder.itemView);
            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), holder.itemView, subreddit, (subreddit.equals("frontpage")||(subreddit.equals("all"))|| subreddit.contains(".") || subreddit.contains("+")));
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    if (Authentication.didOnline || submission.getComments() != null) {
                        holder2.itemView.setAlpha(0.5f);
                        if (SettingValues.tabletUI && sContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            Intent i2 = new Intent(sContext, CommentsScreenPopup.class);
                            i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                            i2.putExtra(CommentsScreenPopup.EXTRA_PAGE, holder2.getAdapterPosition());
                            (sContext).startActivity(i2);

                        } else {
                            Intent i2 = new Intent(sContext, CommentsScreen.class);
                            i2.putExtra(CommentsScreen.EXTRA_PAGE, holder2.getAdapterPosition());
                            i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                            (sContext).startActivity(i2);
                        }
                    } else {
                        Snackbar.make(holder.itemView, R.string.offline_comments_not_loaded, Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
            final boolean saved = submission.isSaved();


            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!dataSet.stillShow) {

                        Snackbar.make(holder.itemView, sContext.getString(R.string.offline_msg), Snackbar.LENGTH_SHORT).show();

                    } else {
                        LayoutInflater inflater = sContext.getLayoutInflater();
                        final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(sContext);
                        final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                        title.setText(Html.fromHtml(submission.getTitle()));

                        ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                        ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                        dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent i = new Intent(sContext, Profile.class);
                                i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                                sContext.startActivity(i);
                            }
                        });

                        dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(sContext, SubredditView.class);
                                i.putExtra(SubredditView.EXTRA_SUBREDDIT, submission.getSubredditName());
                                sContext.startActivity(i);
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
                                OpenRedditLink.customIntentChooser(urlString, sContext);
                            }
                        });
                        dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (submission.isSelfPost())
                                    Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), sContext);
                                else {
                                    new BottomSheet.Builder(sContext, R.style.BottomSheet_Dialog)
                                            .title(R.string.submission_share_title)
                                            .grid()
                                            .sheet(R.menu.share_menu)
                                            .listener(new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case R.id.reddit_url:
                                                            Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), sContext);
                                                            break;
                                                        case R.id.link_url:
                                                            Reddit.defaultShareText(submission.getUrl(), sContext);
                                                            break;
                                                    }
                                                }
                                            }).show();
                                }

                            }
                        });
                        dialoglayout.findViewById(R.id.copy).setVisibility(View.GONE);
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

            new PopulateSubmissionViewHolder().populateSubmissionViewHolder(holder, submission, sContext, false, false, dataSet.posts, listView, custom, !dataSet.stillShow);

            holder.itemView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onLayoutChange(final View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    //setAnimation(v, i);

                }
            });
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
        views.add(holder2);
    }

    public class SubmissionFooterViewHolder extends RecyclerView.ViewHolder {
        public SubmissionFooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        if (dataSet.posts == null || dataSet.posts.size() == 0) {
            return 0;
        } else {
            return dataSet.posts.size() + 1; // Always account for footer
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
            if (position >= Reddit.lastposition.get(Reddit.currentPosition) - 1 && SettingValues.animation) {

                Animation slide_up = AnimationUtils.loadAnimation(sContext,
                        R.anim.slide_up);




                slide_up.setInterpolator(new DecelerateInterpolator());
                viewToAnimate.setVisibility(View.VISIBLE);

                viewToAnimate.startAnimation(slide_up);
                Reddit.lastposition.set(Reddit.currentPosition, Reddit.lastposition.get(Reddit.currentPosition) + 1);
            }
        } catch (IndexOutOfBoundsException e) {
            fixSliding(Reddit.currentPosition);
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