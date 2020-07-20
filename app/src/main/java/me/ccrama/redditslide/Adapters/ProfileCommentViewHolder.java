package me.ccrama.redditslide.Adapters;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.CommentOverflow;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ProfileCommentViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final TextView user;
    public final TextView score;
    public final TextView time;
    public final View gild;
    public final SpoilerRobotoTextView content;
    public final CommentOverflow overflow;

    public ProfileCommentViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.title);
        user = v.findViewById(R.id.user);
        score = (TextView) v.findViewById(R.id.score);
        time = (TextView) v.findViewById(R.id.time);
        gild = v.findViewById(R.id.gildtext);
        content = (SpoilerRobotoTextView) v.findViewById(R.id.content);
        overflow = (CommentOverflow) v.findViewById(R.id.commentOverflow);
    }
}