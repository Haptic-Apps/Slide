package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public ActiveTextView content;
    public TextView time;

    public MessageViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.title);
        content = (ActiveTextView) v.findViewById(R.id.content);
        time = (TextView) v.findViewById(R.id.time);

    }


}
