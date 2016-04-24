package me.ccrama.redditslide.Adapters;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.CommentOverflow;

/**
 * Created by ccrama on 9/17/2015.
 */
class ProfileCommentViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final TextView score;
    public final TextView time;
    public final View gild;
    public final SpoilerRobotoTextView content;
    public final CommentOverflow overflow;

    public ProfileCommentViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.title);
        score = (TextView) v.findViewById(R.id.score);
        time = (TextView) v.findViewById(R.id.time);
        gild = v.findViewById(R.id.gildtext);
        content = (SpoilerRobotoTextView) v.findViewById(R.id.content);
        overflow = (CommentOverflow) v.findViewById(R.id.commentOverflow);

        //Needed for start/end margin adjustments to be on par with Material Design keylines
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = 16 / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).setMarginStart((int) dp);
        ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).setMarginEnd((int) dp);
    }
}