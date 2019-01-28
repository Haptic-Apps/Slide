package me.ccrama.redditslide.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.CommentOverflow;

/**
 * Created by ccrama on 9/17/2015.
 */
public class CommentViewHolder extends RecyclerView.ViewHolder {
    public final TextView childrenNumber;
    public final View dot;
    public final LinearLayout menuArea;
    public final int textColorUp;
    public final TextView content;
    public final int textColorDown;
    public final int textColorRegular;
    public final SpoilerRobotoTextView firstTextView;
    public final CommentOverflow commentOverflow;
    public final View background;
    public final ImageView imageFlair;

    public CommentViewHolder(View v) {
        super(v);
        background = v.findViewById(R.id.background);
        dot = v.findViewById(R.id.dot);
        menuArea = (LinearLayout) v.findViewById(R.id.menuarea);
        childrenNumber = (TextView) v.findViewById(R.id.commentnumber);
        firstTextView = (SpoilerRobotoTextView) v.findViewById(R.id.firstTextView);
        textColorDown = ContextCompat.getColor(v.getContext(), R.color.md_blue_500);
        textColorRegular = firstTextView.getCurrentTextColor();
        textColorUp = ContextCompat.getColor(v.getContext(), R.color.md_orange_500);
        content = (TextView) v.findViewById(R.id.content);
        imageFlair= (ImageView) v.findViewById(R.id.flair);
        commentOverflow = (CommentOverflow) v.findViewById(R.id.commentOverflow);
    }
}
