package me.ccrama.redditslide.Adapters;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

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
    public final View menu;
    public final View mod;
    public final View hide;
    public final View upvote;
    public final View thumbimage;
    public final View secondMenu;
    public final View downvote;
    public final View edit;
    public final HeaderImageLinkView leadImage;
    public final SpoilerRobotoTextView firstTextView;
    public final CommentOverflow commentOverflow;
    public final View save;
    public final TextView flairText;
    public final SpoilerRobotoTextView body;
    public final RelativeLayout innerRelative;

    public SubmissionViewHolder(View v) {
        super(v);
        title = v.findViewById(R.id.title);
        info = v.findViewById(R.id.information);
        hide = v.findViewById(R.id.hide);
        menu = v.findViewById(R.id.menu);
        mod = v.findViewById(R.id.mod);
        downvote = v.findViewById(R.id.downvote);
        upvote = v.findViewById(R.id.upvote);
        leadImage = v.findViewById(R.id.headerimage);
        contentTitle = v.findViewById(R.id.contenttitle);
        secondMenu = v.findViewById(R.id.secondMenu);
        flairText = v.findViewById(R.id.text);
        thumbimage = v.findViewById(R.id.thumbimage2);
        contentURL = v.findViewById(R.id.contenturl);
        save = v.findViewById(R.id.save);
        edit = v.findViewById(R.id.edit);
        body = v.findViewById(R.id.body);
        score = v.findViewById(R.id.score);
        comments = v.findViewById(R.id.comments);
        firstTextView = v.findViewById(R.id.firstTextView);
        commentOverflow = v.findViewById(R.id.commentOverflow);
        innerRelative = v.findViewById(R.id.innerrelative);
    }
}
