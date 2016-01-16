package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Submission;

import java.util.List;

public interface SubmissionDisplay {
    void updateSuccess(List<Submission> submissions, int startIndex);

    void updateOffline(List<Submission> submissions, long cacheTime);

    void updateOfflineError();

    void updateError();
}
