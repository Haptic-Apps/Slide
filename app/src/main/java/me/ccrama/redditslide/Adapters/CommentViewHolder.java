package me.ccrama.redditslide.Adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.CommentOverflow;

/**
 * Created by ccrama on 9/17/2015.
 */
public class CommentViewHolder extends RecyclerView.ViewHolder {
    public final TextView author;
    public final View dots;
    public final TextView time;
    public final View gild;
    public final LinearLayout commentArea;
    public final View children;
    public final TextView childrenNumber;
    public final TextView flairText;
    public final View flairBubble;
    public final View you;
    public final View op;
    public final View dot;
    public final LinearLayout menuArea;
    public final int textColorUp;
    public final TextView score;
    public final int textColorDown;
    public final int textColorRegular;
    public final SpoilerRobotoTextView firstTextView;
    public final CommentOverflow commentOverflow;
    public final View background;

    public CommentViewHolder(View v) {
        super(v);
        background = v.findViewById(R.id.background);
        flairBubble = v.findViewById(R.id.flairbubble);
        flairText = (TextView) flairBubble.findViewById(R.id.text);
        author = (TextView) v.findViewById(R.id.author);
        dot = v.findViewById(R.id.dot);
        menuArea = (LinearLayout) v.findViewById(R.id.menuarea);
        time = (TextView) v.findViewById(R.id.time);
        children = v.findViewById(R.id.children);
        commentArea = (LinearLayout) v.findViewById(R.id.commentArea);
        childrenNumber = (TextView) children.findViewById(R.id.flairtext);
        gild = v.findViewById(R.id.gild);
        firstTextView = (SpoilerRobotoTextView) v.findViewById(R.id.firstTextView);
        textColorDown = ContextCompat.getColor(v.getContext(), R.color.md_blue_500);
        textColorRegular = author.getCurrentTextColor();
        textColorUp = ContextCompat.getColor(v.getContext(), R.color.md_orange_500);
        you = v.findViewById(R.id.you);
        op = v.findViewById(R.id.op);
        dots = v.findViewById(R.id.dots);
        score = (TextView) v.findViewById(R.id.score);
        commentOverflow = (CommentOverflow) v.findViewById(R.id.commentOverflow);
    }
}
