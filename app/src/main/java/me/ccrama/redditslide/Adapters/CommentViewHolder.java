package me.ccrama.redditslide.Adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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
        menuArea = v.findViewById(R.id.menuarea);
        childrenNumber = v.findViewById(R.id.commentnumber);
        firstTextView = v.findViewById(R.id.firstTextView);
        textColorDown = ContextCompat.getColor(v.getContext(), R.color.md_blue_500);
        textColorRegular = firstTextView.getCurrentTextColor();
        textColorUp = ContextCompat.getColor(v.getContext(), R.color.md_orange_500);
        content = v.findViewById(R.id.content);
        imageFlair= v.findViewById(R.id.flair);
        commentOverflow = v.findViewById(R.id.commentOverflow);
    }
}
