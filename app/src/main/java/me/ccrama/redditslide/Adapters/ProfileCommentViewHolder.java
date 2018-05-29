package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.CommentOverflow;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ProfileCommentViewHolder extends RecyclerView.ViewHolder {
    public final TextView              title;
    public final TextView              score;
    public final TextView              time;
    public final View                  gild;
    public final SpoilerRobotoTextView content;
    public final CommentOverflow       overflow;

    public ProfileCommentViewHolder(View v) {
        super(v);
        title = v.findViewById(R.id.title);
        score = v.findViewById(R.id.score);
        time = v.findViewById(R.id.time);
        gild = v.findViewById(R.id.gildtext);
        content = v.findViewById(R.id.content);
        overflow = v.findViewById(R.id.commentOverflow);
    }
}