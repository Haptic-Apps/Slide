package me.ccrama.redditslide.Adapters;

/**
 * Created by carlo_000 on 3/22/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;


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

                Intent i2 = new Intent(mContext, CommentsScreen.class);
                DataShare.sharedSubreddit = dataSet.posts;
                i2.putExtra("page",i );
                ((Activity) mContext).startActivityForResult(i2, 2);


            }
        });

        new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder(holder, submission, mContext, false);

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