package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.Views.CreateCardView;

public class MultiredditAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BaseAdapter {

    public final Activity mContext;
    private final MultiredditPosts dataSet;
    private final RecyclerView listView;
    private final int SPACER = 6;
    public boolean spanned;

    public MultiredditAdapter(Activity mContext, MultiredditPosts dataSet, RecyclerView listView, SwipeRefreshLayout refreshLayout) {
        this.mContext = mContext;
        this.refreshLayout = refreshLayout;
        this.listView = listView;
        this.dataSet = dataSet;
        spanned = listView.getLayoutManager() instanceof StaggeredGridLayoutManager;
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
            position -= 1;
        }
        if (position == dataSet.posts.size() && dataSet.posts.size() != 0) {
            return 5;
        }
        return 1;
    }

    SwipeRefreshLayout refreshLayout;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == SPACER) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spacer, viewGroup, false);
            return new SpacerViewHolder(v);

        } else if (i == 5) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loadingmore, viewGroup, false);
            return new ContributionAdapter.EmptyViewHolder(v);
        } else {
            View v = CreateCardView.CreateView(viewGroup, false, "nomatching");
            return new SubmissionViewHolder(v);
        }
    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder2, int pos) {

        int i = pos != 0 ? pos - 1 : pos;
        if (holder2 instanceof SubmissionViewHolder) {
            final SubmissionViewHolder holder = (SubmissionViewHolder) holder2;
            final Submission submission = dataSet.posts.get(i);
            CreateCardView.resetColorCard(holder.itemView);
            CreateCardView.colorCard(submission.getSubredditName().toLowerCase(), holder.itemView, "nomatching", true);
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    holder.title.setAlpha(0.65f);
                    holder.leadImage.setAlpha(0.65f);
                    holder.thumbimage.setAlpha(0.65f);

                    Intent i2 = new Intent(mContext, CommentsScreen.class);
                    i2.putExtra(CommentsScreen.EXTRA_PAGE, holder2.getAdapterPosition() - 1);
                    i2.putExtra(CommentsScreen.EXTRA_MULTIREDDIT, dataSet.getMultiReddit().getDisplayName());
                    mContext.startActivityForResult(i2, 2);


                }
            });

            new PopulateSubmissionViewHolder().populateSubmissionViewHolder(holder, submission, mContext, false, false, dataSet.posts, listView, false, false, "multi" + dataSet.getMultiReddit().getDisplayName());
        }
        if (holder2 instanceof SpacerViewHolder) {
            holder2.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(holder2.itemView.getWidth(), (mContext).findViewById(R.id.header).getHeight()));
            if (spanned) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.setFullSpan(true);
                holder2.itemView.setLayoutParams(layoutParams);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (dataSet.posts == null || dataSet.posts.size() == 0) {
            return 0;
        } else {
            return dataSet.posts.size() + 2;
        }
    }


}