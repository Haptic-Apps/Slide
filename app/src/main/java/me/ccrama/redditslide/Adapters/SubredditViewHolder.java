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
public class SubredditViewHolder extends RecyclerView.ViewHolder {
    public final SpoilerRobotoTextView body;
    public final CommentOverflow overflow;
    public final View color;
    public final TextView name;
    public final TextView subscribers;
    public final View subbed;

    public SubredditViewHolder(View v) {
        super(v);
        color = v.findViewById(R.id.color);
        name = (TextView) v.findViewById(R.id.name);
        subscribers = v.findViewById(R.id.subscribers);
        subbed = v.findViewById(R.id.subbed);
        body = (SpoilerRobotoTextView) v.findViewById(R.id.body);
        overflow = (CommentOverflow) v.findViewById(R.id.overflow);
    }
}
