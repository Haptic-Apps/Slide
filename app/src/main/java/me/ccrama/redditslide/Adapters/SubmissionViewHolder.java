package me.ccrama.redditslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubmissionViews.HeaderImageLinkView;
import me.ccrama.redditslide.Views.CommentOverflow;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubmissionViewHolder extends RecyclerView.ViewHolder {
    public final SpoilerRobotoTextView title;
    public final TextView contentTitle;
    public final TextView contentURL;
    public final TextView score;
    public final TextView comments;
    public final TextView info;
    public final TextView gildText;
    public final View gildLayout;
    public final HeaderImageLinkView leadImage;
    public final SpoilerRobotoTextView firstTextView;
    public final CommentOverflow commentOverflow;

    public SubmissionViewHolder(View v) {
        super(v);
        title = (SpoilerRobotoTextView) v.findViewById(R.id.title);
        info = (TextView) v.findViewById(R.id.information);
        leadImage = (HeaderImageLinkView) v.findViewById(R.id.headerimage);
        contentTitle = (TextView) v.findViewById(R.id.contenttitle);
        contentURL = (TextView) v.findViewById(R.id.contenturl);
        score = (TextView) v.findViewById(R.id.score);
        comments = (TextView) v.findViewById(R.id.comments);
        gildText = (TextView)v.findViewById(R.id.gildtext);
        gildLayout = v.findViewById(R.id.gild);
        firstTextView = (SpoilerRobotoTextView) v.findViewById(R.id.firstTextView);
        commentOverflow = (CommentOverflow) v.findViewById(R.id.commentOverflow);
    }
}
