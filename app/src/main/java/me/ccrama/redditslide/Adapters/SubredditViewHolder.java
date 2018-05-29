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
public class SubredditViewHolder extends RecyclerView.ViewHolder {
    public final SpoilerRobotoTextView body;
    public final CommentOverflow       overflow;
    public final View                  color;
    public final TextView              name;
    public final View                  subbed;

    public SubredditViewHolder(View v) {
        super(v);
        color = v.findViewById(R.id.color);
        name = v.findViewById(R.id.name);
        subbed = v.findViewById(R.id.subbed);
        body = v.findViewById(R.id.body);
        overflow = v.findViewById(R.id.overflow);
    }
}
