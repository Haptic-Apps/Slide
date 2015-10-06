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
    public TextView title;
    public TextView score;
    public TextView author;
    public LinearLayout dots;
    public TextView time;
    public View loadMore;
    public TextView loadMoreText;
    public View gild;
    public View children;
    public View more;
    public View save;
    public View upvote;
    public View downvote;
    public View reply;
    public View replyArea;
    public View menu;
    public ActiveTextView content;
    public View discard;
    public View send;
    public EditText replyLine;


    public CommentViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.title);
        author = (TextView) v.findViewById(R.id.author);
        loadMore= v.findViewById(R.id.loadMore);
        loadMoreText = (TextView) v.findViewById(R.id.loadMoreText);
        score = (TextView) v.findViewById(R.id.score);
        time = (TextView) v.findViewById(R.id.time);
        gild =  v.findViewById(R.id.gild);
        children =  v.findViewById(R.id.children);
        content = (ActiveTextView) v.findViewById(R.id.content);

        dots = (LinearLayout) v.findViewById(R.id.dots);
        replyLine = (EditText) v.findViewById(R.id.replyLine);
        more =  v.findViewById(R.id.more);
        save =  v.findViewById(R.id.save);
        upvote =  v.findViewById(R.id.upvote);
        downvote =  v.findViewById(R.id.downvote);
        reply =  v.findViewById(R.id.reply);
        replyArea = v.findViewById(R.id.replyArea);
        menu = v.findViewById(R.id.menu);
        discard =  v.findViewById(R.id.discard);
        send =  v.findViewById(R.id.send);

    }


}
