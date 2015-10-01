package me.ccrama.redditslide.Adapters;

/**
 * Created by carlo_000 on 3/22/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.PopulateSubmissionViewHolder;


public class ContributionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public Context mContext;
    public ArrayList<Contribution> dataSet;
    RecyclerView listView;


    static int COMMENT = 1;
    @Override
    public int getItemViewType(int position) {
        if (dataSet.get(position).getFullName().contains("t1"))//IS COMMENT
            return COMMENT;

        return 2;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        if (i == COMMENT) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_comment, viewGroup, false);
            return new ProfileCommentViewHolder(v);
        } else {
            View v = CreateCardView.CreateView(viewGroup);
            return new SubmissionViewHolder(v);

        }

    }




    public ContributionAdapter(Context mContext, ContributionPosts dataSet, RecyclerView listView) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet.posts;

        isSame = false;

    }


    boolean isSame;


    int lastPosition = -1;

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHolder, final int i) {

        if (firstHolder instanceof SubmissionViewHolder) {
            SubmissionViewHolder holder = (SubmissionViewHolder) firstHolder;
            final Submission submission = (Submission) dataSet.get(i);
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
/*TODO Single comment screen
                    Intent i2 = new Intent(mContext, CommentsScreen.class);
                    DataShare.sharedSubreddit = dataSet;
                    i2.putExtra("page", i);
                    ((Activity) mContext).startActivityForResult(i2, 2);
*/

                }
            });

            new PopulateSubmissionViewHolder().PopulateSubmissionViewHolder(holder, submission, mContext, false);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OpenRedditLink(mContext, "www.reddit.com" + submission.getPermalink());
                }
            });

            lastPosition = i;
        } else {
            //IS COMMENT
            ProfileCommentViewHolder holder = (ProfileCommentViewHolder) firstHolder;
            final Comment comment = (Comment) dataSet.get(i);
            holder.score.setText(comment.getScore() + "");

            holder.time.setText(TimeUtils.getTimeAgo(comment.getCreatedUtc().getTime()));

            new MakeTextviewClickable().ParseTextWithLinksTextViewComment(comment.getDataNode().get("body_html").asText(), holder.content, (Activity) mContext);
            if (comment.getTimesGilded() > 0) {
                holder.gild.setVisibility(View.VISIBLE);
            } else {
                holder.gild.setVisibility(View.GONE);
            }
           holder.title.setText(comment.getSubmissionTitle());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OpenRedditLink(mContext, comment.getSubmissionId(), comment.getSubredditName(), comment.getId());
                }
            });
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OpenRedditLink(mContext, comment.getSubmissionId(), comment.getSubredditName(), comment.getId());
                }
            });

        }
        return;

    }

    @Override
    public int getItemCount() {
        if (dataSet == null) {
            return 0;
        } else {
            return dataSet.size();
        }
    }


}