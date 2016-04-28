package me.ccrama.redditslide;

/**
 * Constants used throughout the app
 */
public class Constants {
    // Maximum posts to request from Reddit
    public static final int PAGINATOR_POST_LIMIT = 25;

    /**
     * Top margin of the SubmissionsView, MultiredditView, ModeratorView, and DiscoverView.
     * This is used in the following Adapters: Submission, Subreddits, Multireddit, Moderator.
     * This value is measured in dp units, and will need to be converted to px units before use.
     */
    public static final int TOP_MARGIN_SUBMISSIONS_OFFSET = 24;
}
