package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Submission;

import java.util.List;

public interface SubmissionDisplay {
    void update(List<Submission> submissions, boolean reset, boolean offline, String subreddit);
}
