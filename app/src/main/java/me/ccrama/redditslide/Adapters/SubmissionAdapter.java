package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import it.sephiroth.android.library.tooltip.Tooltip;
import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.Palette;


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
            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), holder.itemView, subreddit, (subreddit.equals("frontpage") || (subreddit.equals("all")) || subreddit.contains(".") || subreddit.contains("+")));
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    if (Authentication.didOnline || submission.getComments() != null) {
                        holder2.itemView.setAlpha(0.5f);
                        if (SettingValues.tabletUI && holder2.itemView.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            Intent i2 = new Intent(holder2.itemView.getContext(), CommentsScreenPopup.class);
                            i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                            i2.putExtra(CommentsScreenPopup.EXTRA_PAGE, holder2.getAdapterPosition());
                            (holder2.itemView.getContext()).startActivity(i2);

                        } else {
                            Intent i2 = new Intent(holder2.itemView.getContext(), CommentsScreen.class);
                            i2.putExtra(CommentsScreen.EXTRA_PAGE, holder2.getAdapterPosition());
                            i2.putExtra(CommentsScreen.EXTRA_SUBREDDIT, subreddit);
                            (holder2.itemView.getContext()).startActivity(i2);
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

                    if (context instanceof MainActivity && ((MainActivity) context).t != null) {
                        Tooltip.removeAll(context);
                        Reddit.appRestart.edit().putString("tutorial_4", "A").apply();
                    }
                    if (!dataSet.stillShow) {

                        Snackbar.make(holder.itemView, holder2.itemView.getContext().getString(R.string.offline_msg), Snackbar.LENGTH_SHORT).show();

                    } else {
                        LayoutInflater inflater = LayoutInflater.from(holder2.itemView.getContext());
                        final View dialoglayout = inflater.inflate(R.layout.postmenu, null);
                        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(holder2.itemView.getContext());
                        final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                        title.setText(Html.fromHtml(submission.getTitle()));

                        ((TextView) dialoglayout.findViewById(R.id.userpopup)).setText("/u/" + submission.getAuthor());
                        ((TextView) dialoglayout.findViewById(R.id.subpopup)).setText("/r/" + submission.getSubredditName());
                        dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent i = new Intent(holder2.itemView.getContext(), Profile.class);
                                i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                                holder2.itemView.getContext().startActivity(i);
                            }
                        });

                        dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(holder2.itemView.getContext(), SubredditView.class);
                                i.putExtra(SubredditView.EXTRA_SUBREDDIT, submission.getSubredditName());
                                holder2.itemView.getContext().startActivity(i);
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
                                OpenRedditLink.customIntentChooser(urlString, holder2.itemView.getContext());
                            }
                        });
                        dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (submission.isSelfPost())
                                    Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), holder2.itemView.getContext());
                                else {
                                    new BottomSheet.Builder(holder2.itemView.getContext(), R.style.BottomSheet_Dialog)
                                            .title(R.string.submission_share_title)
                                            .grid()
                                            .sheet(R.menu.share_menu)
                                            .listener(new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case R.id.reddit_url:
                                                            Reddit.defaultShareText("https://reddit.com" + submission.getPermalink(), holder2.itemView.getContext());
                                                            break;
                                                        case R.id.link_url:
                                                            Reddit.defaultShareText(submission.getUrl(), holder2.itemView.getContext());
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
                        if (!SettingValues.hideButton) {
                            dialoglayout.findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final int pos = dataSet.posts.indexOf(submission);
                                    final Submission old = dataSet.posts.get(pos);
                                    dataSet.posts.remove(submission);

                                    notifyItemRemoved(pos);
                                    d.dismiss();
                                    Hidden.setHidden(old);

                                    OfflineSubreddit.getSubreddit(dataSet.subreddit).hide(pos);


                                    Snackbar.make(listView, R.string.submission_info_hidden, Snackbar.LENGTH_LONG).setAction(R.string.btn_undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            OfflineSubreddit.getSubreddit(dataSet.subreddit).unhideLast();

                                            dataSet.posts.add(pos, old);
                                            notifyItemInserted(pos);
                                            Hidden.undoHidden(old);

                                        }
                                    }).show();
                                }
                            });
                        } else {
                            dialoglayout.findViewById(R.id.hide).setVisibility(View.GONE);
                        }


                    }
                    return true;
                }

            });


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


    static void fixSliding(int position) {
        try {
            Reddit.lastposition.add(position, 0);
        } catch (IndexOutOfBoundsException e) {
            fixSliding(position - 1);
        }
    }
}