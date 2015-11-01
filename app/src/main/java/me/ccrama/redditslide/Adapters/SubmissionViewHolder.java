package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubmissionViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final TextView subreddit;
    public final ImageView leadImage;
    public final TextView textImage;
    public final TextView subTextImage;
    public final View imageArea;
    public final TextView contentTitle;
    public final TextView contentURL;
    public final ImageView thumbImage;
    public final View previewContent;
    public final TextView score;
    public final TextView comments;
    public final TextView info;

    public SubmissionViewHolder(View v) {
        super(v);
        title = (TextView) v.findViewById(R.id.title);
        info = (TextView) v.findViewById(R.id.information);
        subreddit = (TextView) v.findViewById(R.id.subreddit);
        imageArea = v.findViewById(R.id.imagearea);
        leadImage = (ImageView) v.findViewById(R.id.leadimage);
        thumbImage = (ImageView) v.findViewById(R.id.thumbimage);

        textImage = (TextView) v.findViewById(R.id.textimage);
        subTextImage = (TextView) v.findViewById(R.id.subtextimage);
        contentTitle = (TextView) v.findViewById(R.id.contenttitle);
        contentURL = (TextView) v.findViewById(R.id.contenturl);
        score = (TextView) v.findViewById(R.id.score);
        comments = (TextView) v.findViewById(R.id.comments);
        View actionBar = v.findViewById(R.id.actionbar);
        previewContent = v.findViewById(R.id.previewContent);

    }


}
