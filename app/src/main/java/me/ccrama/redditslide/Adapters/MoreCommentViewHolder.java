package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;

/**
 * Created by ccrama on 9/17/2015.
 */
public class MoreCommentViewHolder extends RecyclerView.ViewHolder {
    public final LinearLayout dots;

    public final SpoilerRobotoTextView content;
    public final View dot;


    public MoreCommentViewHolder(View v) {
        super(v);

        dot = v.findViewById(R.id.dot);

        content = (SpoilerRobotoTextView) v.findViewById(R.id.content);


        dots = (LinearLayout) v.findViewById(R.id.dots);

    }


}
