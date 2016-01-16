package me.ccrama.redditslide;

import android.content.Context;

import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.Adapters.SubmissionDisplay;

/**
 * Created by Deadl on 16/01/2016.
 */
public interface PostLoader {
    void loadMore(Context context, SubmissionDisplay displayer, boolean reset);

    List<Submission> getPosts();

    boolean hasMore();
}
