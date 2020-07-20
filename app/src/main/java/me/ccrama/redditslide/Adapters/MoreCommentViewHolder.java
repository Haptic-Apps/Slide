package me.ccrama.redditslide.Adapters;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class MoreCommentViewHolder extends RecyclerView.ViewHolder {
    public final TextView content;
    public final View loading;
    public final View dots;

    public MoreCommentViewHolder(View v) {
        super(v);
        dots = v.findViewById(R.id.dot);
        content = (TextView) v.findViewById(R.id.content);
        loading = v.findViewById(R.id.loading);
    }
}
