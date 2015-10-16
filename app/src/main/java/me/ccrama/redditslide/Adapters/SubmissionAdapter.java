package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;


public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionViewHolder> {

    public Context mContext;
    public ArrayList<Submission> dataSet;
    RecyclerView listView;

    public String subreddit;


    @Override
    public SubmissionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = CreateCardView.CreateView( viewGroup, subreddit);

        return new SubmissionViewHolder(v);

    }

    public SubmissionAdapter(Context mContext, SubredditPosts dataSet, RecyclerView listView, String subreddit) {

        this.mContext = mContext;
        this.subreddit = subreddit;
        this.listView = listView;
        this.dataSet = dataSet.posts;

        isSame = false;

    }


    boolean isSame;


    int lastPosition = -1;


    @Override
    public void onBindViewHolder(final SubmissionViewHolder holder, final int i) {

        final Submission submission = dataSet.get(i);
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                DataShare.sharedSubreddit = dataSet;

                if(Reddit.tabletUI && mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ){
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

        new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder(holder, submission, mContext, false);

        lastPosition = i;

    }

    @Override
    public int getItemCount() {
        if(dataSet == null)
        { return 0;} else {
            return dataSet.size();
        }
    }


}