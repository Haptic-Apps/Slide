package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Submission;

import java.util.List;

/**
 * Interface to provide methods for updating an object when new submissions
 * have been loaded.
 */
public interface SubmissionDisplay {
    /**
     * Called when the update was done online.
     *
     * @param submissions the updated list of submissions
     * @param startIndex  the index of the first new submission
     */
    void updateSuccess(List<Submission> submissions, int startIndex);

    /**
     * Called when the update was offline.
     *
     * @param submissions the updated list of submissions
     * @param cacheTime   the last time updated (unix time?)
     */
    void updateOffline(List<Submission> submissions, long cacheTime);

    /**
     * Called when the update was offline but failed (e.g. no subreddit was cached).
     */
    void updateOfflineError();

    /**
     * Called when the update was done online but failed (e.g. network connection).
     */
    void updateError();

    void updateViews();

    void onAdapterUpdated();
}
