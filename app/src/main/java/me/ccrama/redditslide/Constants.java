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
     * This is used in conjunction with the multiplication of a percentage to adjust an offset.
     * This is used when determining the offset for the SwipeRefreshLayout loading progress.
     */
    public static final int SCREEN_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
}
