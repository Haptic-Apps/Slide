package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class CommentViewHolder extends RecyclerView.ViewHolder {
    private final TextView title;
    public final TextView score;
    public final TextView author;
    public final LinearLayout dots;
    public final TextView time;
    public final View gild;
    public final View children;
    public final View more;
    public final View save;
    public final View upvote;
    public final View downvote;
    public final View reply;
    public final View replyArea;
    public final View menu;
    public final ActiveTextView content;
    public final View discard;
    public final View send;
    public final EditText replyLine;

    public final TextView childrenNumber;
    public final TextView flairText;
    public final View flairBubble;
    public final View you;
    public final View op;
    public final View dot;
    public final int textColorUp;
    public final int textColorDown;
    public final int textColorRegular;

    public CommentViewHolder(View v) {
        super(v);
        flairBubble = v.findViewById(R.id.flairbubble);
        title = (TextView) v.findViewById(R.id.title);
        flairText = (TextView) flairBubble.findViewById(R.id.text);
        author = (TextView) v.findViewById(R.id.author);
        score = (TextView) v.findViewById(R.id.score);
        dot = v.findViewById(R.id.dot);
        time = (TextView) v.findViewById(R.id.time);
        children = v.findViewById(R.id.children);
        childrenNumber = (TextView) children.findViewById(R.id.flairtext);
        gild = v.findViewById(R.id.gild);
        content = (ActiveTextView) v.findViewById(R.id.content);

        textColorDown = v.getContext().getResources().getColor(R.color.md_blue_500);
        textColorRegular = author.getCurrentTextColor();
        textColorUp = v.getContext().getResources().getColor(R.color.md_orange_500);
        you = v.findViewById(R.id.you);
        op = v.findViewById(R.id.op);
        dots = (LinearLayout) v.findViewById(R.id.dots);
        replyLine = (EditText) v.findViewById(R.id.replyLine);
        more = v.findViewById(R.id.more);
        save = v.findViewById(R.id.save);
        upvote = v.findViewById(R.id.upvote);
        downvote = v.findViewById(R.id.downvote);
        reply = v.findViewById(R.id.reply);
        replyArea = v.findViewById(R.id.replyArea);
        menu = v.findViewById(R.id.menu);
        discard = v.findViewById(R.id.discard);
        send = v.findViewById(R.id.send);

    }


}
