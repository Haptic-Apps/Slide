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

    /**
     * These offsets are used for the SwipeToRefresh (PTR) progress indicator.
     * The TOP offset is used for the starting point of the indicator (underneath the toolbar).
     * The BOTTOM offset is used for the end point of the indicator (below the toolbar).
     * This is used whenever we call mSwipeRefreshLayout.setProgressViewOffset().
     * This values are in px units, and must be converted to dp before use.
     */
    public static final int PTR_OFFSET_TOP = 44;
    public static final int PTR_OFFSET_BOTTOM = 16;

}
