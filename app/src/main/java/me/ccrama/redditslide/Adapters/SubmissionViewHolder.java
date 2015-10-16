package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.ccrama.redditslide.R;

public class SubmissionViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public TextView subreddit;
    public ImageView leadImage;
    public TextView textImage;
    public TextView subTextImage;
    public View imageArea;
    public TextView contentTitle;
    public TextView contentURL;
    public ImageView thumbImage;
    public View previewContent;
    public TextView score;
    public TextView comments;
    public View actionBar;
    public TextView info;

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
        actionBar = v.findViewById(R.id.actionbar);
        previewContent = v.findViewById(R.id.previewContent);

    }


}
