package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.ApiException;
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


public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionViewHolder> {

    public Context mContext;
    public ArrayList<Submission> dataSet;
    RecyclerView listView;

    public String subreddit;

    boolean custom;


    @Override
    public SubmissionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = CreateCardView.CreateView( viewGroup, custom, subreddit.toLowerCase());

        return new SubmissionViewHolder(v);

    }

    public SubmissionAdapter(Context mContext, SubredditPosts dataSet, RecyclerView listView, String subreddit) {

        this.mContext = mContext;
        this.subreddit = subreddit;
        this.listView = listView;
        this.dataSet = dataSet.posts;

        if(SettingValues.prefs.contains("PRESET" + subreddit)){
            custom = true;
        } else {
            custom = false;
        }

        isSame = false;

    }



    boolean isSame;


    int lastPosition = -1;

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
                    Snackbar.make(v, "Submission unsaved", Snackbar.LENGTH_SHORT).show();

                    submissions[0].saved = false;
                    v = null;

                } else {
                    new AccountManager(Authentication.reddit).save(submissions[0]);
                    Snackbar.make(v, "Submission saved", Snackbar.LENGTH_SHORT).show();

                    submissions[0].saved = true;
                    v = null;


                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBindViewHolder(final SubmissionViewHolder holder, final int i) {

        final Submission submission = dataSet.get(i);
        CreateCardView.resetColorCard(holder.itemView);
                CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), holder.itemView, subreddit, custom);
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DataShare.sharedSubreddit = dataSet;

                if (Reddit.tabletUI && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Intent i2 = new Intent(mContext, CommentsScreenPopup.class);
                    i2.putExtra("page", i);
                    (mContext).startActivity(i2);

                } else {
                    Intent i2 = new Intent(mContext, CommentsScreen.class);
                    i2.putExtra("page", i);
                    (mContext).startActivity(i2);
                }

            }
        });
        final boolean saved = submission.isSaved();

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
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
                        new AsyncSave(holder.itemView).execute(submission);

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
                        Reddit.defaultShareText("http://reddit.com" + submission.getPermalink(), mContext);

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
                        final int pos = dataSet.indexOf(submission);
                        final Submission old = dataSet.get(pos);
                        dataSet.remove(submission);
                        notifyItemRemoved(pos);
                        d.dismiss();
                        Hidden.setHidden( old);

                        Snackbar.make(listView, "Post hidden forever.", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dataSet.add(pos, old);
                                notifyItemInserted(pos);
                                Hidden.undoHidden(old);

                            }
                        }).show();


                    }
                });                return true;
            }
        });

        new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder(holder, submission, mContext, false, false, dataSet, listView);

        lastPosition = i;
        return;

    }

    @Override
    public int getItemCount() {
        if(dataSet == null)
        { return 0;} else {
            return dataSet.size();
        }
    }


}