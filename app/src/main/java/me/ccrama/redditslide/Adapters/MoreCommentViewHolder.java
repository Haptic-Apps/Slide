package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class MoreCommentViewHolder extends RecyclerView.ViewHolder {

    public final View dot;


    public MoreCommentViewHolder(View v) {
        super(v);

        dot = v.findViewById(R.id.dot);


    }


}
