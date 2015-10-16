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
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Visuals.Pallete;


public class MultiredditAdapter extends RecyclerView.Adapter<SubmissionViewHolder> {

    public Context mContext;
    public MultiredditPosts dataSet;
    RecyclerView listView;



    @Override
    public SubmissionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = CreateCardView.CreateView(viewGroup);
        return new SubmissionViewHolder(v);
    }

    public MultiredditAdapter(Context mContext, MultiredditPosts dataSet, RecyclerView listView) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        isSame = false;

    }

    boolean isSame;


    int lastPosition = -1;

    @Override
    public void onBindViewHolder(final SubmissionViewHolder holder, final int i) {

        final Submission submission = dataSet.posts.get(i);
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DataShare.sharedSubreddit = dataSet.posts;

                if(Reddit.tabletUI && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ){
                    Intent i2 = new Intent(mContext, CommentsScreenPopup.class);
                    i2.putExtra("page", i);
                    (mContext).startActivity(i2);

                } else {
                    Intent i2 = new Intent(mContext, CommentsScreen.class);
                    i2.putExtra("page", i);
                    ((Activity) mContext).startActivityForResult(i2, 2);
                }


            }
        });

        new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder(holder, submission, mContext, false, false, dataSet.posts, listView);
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
                        if (submission.isSaved()) {
                            ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText("Save post");
                        } else {
                            ((TextView) dialoglayout.findViewById(R.id.savedtext)).setText("Post saved");

                        }
                        new SubmissionAdapter.AsyncSave(holder.itemView).execute(submission);

                    }
                });
                if (submission.isSaved()) {
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
                final Dialog d = builder.show();
                dialoglayout.findViewById(R.id.hide).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int pos = dataSet.posts.indexOf(submission);
                        final Submission old  = dataSet.posts.get(pos);
                        dataSet.posts.remove(submission);
                        notifyItemRemoved(pos);
                        d.dismiss();
                        Hidden.setHidden(old);

                        Snackbar.make(listView, "Post hidden forever.", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dataSet.posts.add(pos, old);
                                notifyItemInserted(pos);
                                Hidden.undoHidden(old);

                            }
                        }).show();


                    }
                });                return true;
            }
        });
        lastPosition = i;
        return;

    }

    @Override
    public int getItemCount() {
        if(dataSet == null || dataSet.posts == null)
        { return 0;} else {
            return dataSet.posts.size();
        }
    }


}