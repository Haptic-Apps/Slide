package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class MessageViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final ActiveTextView content;
    public final TextView time;
    public final TextView user;

    public MessageViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.title);
        content = (ActiveTextView) v.findViewById(R.id.content);
        time = (TextView) v.findViewById(R.id.time);

        user = (TextView) v.findViewById(R.id.user);
    }
}
