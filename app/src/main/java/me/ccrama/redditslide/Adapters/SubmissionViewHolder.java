package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rey.material.widget.ProgressView;

import java.net.URL;
import java.util.UUID;

import me.ccrama.redditslide.ForceTouch.PeekViewActivity;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubmissionViews.AutoPlayManager;
import me.ccrama.redditslide.SubmissionViews.HeaderImageLinkView;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.MediaVideoView;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubmissionViewHolder extends RecyclerView.ViewHolder implements
        AutoPlayManager.AutoPlayer {
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
    public final ProgressBar progress;
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

    public final String id = UUID.randomUUID().toString();

    public SubmissionViewHolder(View v) {
        super(v);
        title = (SpoilerRobotoTextView) v.findViewById(R.id.title);
        info = (TextView) v.findViewById(R.id.information);
        hide = v.findViewById(R.id.hide);
        menu = v.findViewById(R.id.menu);
        mod = v.findViewById(R.id.mod);
        downvote = v.findViewById(R.id.downvote);
        upvote = v.findViewById(R.id.upvote);
        leadImage = (HeaderImageLinkView) v.findViewById(R.id.headerimage);
        progress = (ProgressBar) leadImage.findViewById(R.id.progress);
        contentTitle = (TextView) v.findViewById(R.id.contenttitle);
        secondMenu = v.findViewById(R.id.secondMenu);
        flairText = (TextView) v.findViewById(R.id.text);
        thumbimage = v.findViewById(R.id.thumbimage2);
        contentURL = (TextView) v.findViewById(R.id.contenturl);
        save = v.findViewById(R.id.save);
        edit = v.findViewById(R.id.edit);
        body = (SpoilerRobotoTextView) v.findViewById(R.id.body);
        score = (TextView) v.findViewById(R.id.score);
        comments = (TextView) v.findViewById(R.id.comments);
        firstTextView = (SpoilerRobotoTextView) v.findViewById(R.id.firstTextView);
        commentOverflow = (CommentOverflow) v.findViewById(R.id.commentOverflow);
        innerRelative = (RelativeLayout) v.findViewById(R.id.innerrelative);
    }

    boolean isVideoLoaded;
    boolean canLoadVideo;
    boolean canPlayVideo;
    GifUtils.AsyncLoadGif loader;

    @Override
    public void resume() {
        canPlayVideo = true;
        if(isVideoLoaded && canLoadVideo) {
            leadImage.videoContainer.resume();
        }
    }

    @Override
    public void pause() {
        canPlayVideo = false;
        leadImage.videoContainer.pause();
    }

    @Override
    public void beginLoad() {
        canLoadVideo = leadImage.canLoadVideo;
        canPlayVideo = true;
        if(canLoadVideo){
            isVideoLoaded = false;
        }
        progress.setVisibility(View.VISIBLE);
        Activity activity = null;
        final Context context = getView().getContext();
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof android.support.v7.view.ContextThemeWrapper) {
            activity =
                    (Activity) ((android.support.v7.view.ContextThemeWrapper) context).getBaseContext();
        } else if (context instanceof ContextWrapper) {
            Context context1 = ((ContextWrapper) context).getBaseContext();
            if (context1 instanceof Activity) {
                activity = (Activity) context1;
            } else if (context1 instanceof ContextWrapper) {
                Context context2 = ((ContextWrapper) context1).getBaseContext();
                if (context2 instanceof Activity) {
                    activity = (Activity) context2;
                } else if (context2 instanceof ContextWrapper) {
                    activity =
                            (Activity) ((android.support.v7.view.ContextThemeWrapper) context2).getBaseContext();
                }
            }
        } else {
            throw new RuntimeException("Could not find activity from context:" + context);
        }

        loader = new GifUtils.AsyncLoadGif(activity,
                getView(), progress, null, false, true, false, "") {
            @Override
            public void onError() {
                canLoadVideo = false;
            }

            @Override
            public void showGif(URL url, int tries, String subreddit) {
                super.showGif(url, tries, subreddit);
                if(canPlayVideo){
                    getView().start();
                    LogUtil.v("Playing video");
                    leadImage.backdrop.setVisibility(View.GONE);
                }
            }
        };
        LogUtil.v("Video id is " + leadImage.videoURL);
        loader.execute(leadImage.videoURL);

    }

    @Override
    public MediaVideoView getView() {
        return leadImage.videoContainer;
    }

    @Override
    public void kill() {
        canPlayVideo = false;
        if (loader != null) loader.cancel(true);
        leadImage.videoContainer.pause();
    }
}
