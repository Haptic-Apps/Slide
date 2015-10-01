package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.R;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class ProfileCommentViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public TextView score;
    public TextView time;
    public View gild;
    public ActiveTextView content;


    public ProfileCommentViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.title);
        score = (TextView) v.findViewById(R.id.score);
        time = (TextView) v.findViewById(R.id.time);
        gild =  v.findViewById(R.id.gild);
        content = (ActiveTextView) v.findViewById(R.id.content);

    }


}
