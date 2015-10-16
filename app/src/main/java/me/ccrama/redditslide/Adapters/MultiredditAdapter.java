package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Activities.CommentsScreenPopup;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Reddit;
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

        new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder(holder, submission, mContext, false, false);

        lastPosition = i;

    }

    @Override
    public int getItemCount() {
        if(dataSet == null || dataSet.posts == null)
        { return 0;} else {
            return dataSet.posts.size();
        }
    }


}