package me.ccrama.redditslide;

import android.content.res.Resources;

/**
 * Constants used throughout the app
 */
public class Constants {
    // Maximum posts to request from Reddit
    public static final int PAGINATOR_POST_LIMIT = 25;

    /**
     * Height of the device's display, in px.
     * This is used in conjunction with the multiplication of a percentage to adjust various UI
     * component's offsets. For example, this is used when determining the offset to use for the
     * SwipeRefreshLayout progress offset.
     *
     * This is used in the following adapters: Submission, Subreddits, Multireddit, Moderator.
     */
    public static final int SCREEN_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
}
