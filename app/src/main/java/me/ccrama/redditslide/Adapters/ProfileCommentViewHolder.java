package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;

/**
 * Created by ccrama on 9/17/2015.
 */
class ProfileCommentViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final TextView score;
    public final TextView time;
    public final View gild;
    public final SpoilerRobotoTextView content;
    public final LinearLayout overflow;

    public ProfileCommentViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.title);
        score = (TextView) v.findViewById(R.id.score);
        time = (TextView) v.findViewById(R.id.time);
        gild = v.findViewById(R.id.gild);
        content = (SpoilerRobotoTextView) v.findViewById(R.id.content);
        overflow = (LinearLayout) v.findViewById(R.id.commentOverflow);
    }
}