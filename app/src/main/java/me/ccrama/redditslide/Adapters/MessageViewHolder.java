package me.ccrama.redditslide.Adapters;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Views.CommentOverflow;

/**
 * Created by ccrama on 9/17/2015.
 */
public class MessageViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final SpoilerRobotoTextView content;
    public final TextView time;
    public final TextView user;
    public final CommentOverflow commentOverflow;

    public MessageViewHolder(View v) {
        super(v);
        title = v.findViewById(R.id.title);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        content = v.findViewById(R.id.content);
        time = v.findViewById(R.id.time);
        commentOverflow = v.findViewById(R.id.commentOverflow);
        user = v.findViewById(R.id.user);
    }


}
