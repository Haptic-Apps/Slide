package me.ccrama.redditslide;

import android.text.style.URLSpan;
import android.view.MotionEvent;

public interface ClickableText {
    /**
     * Callback for when a link is clicked
     *
     * @param url     the url link (e.g. #s for some spoilers)
     * @param xOffset the last index of the url text (not the link)
     * @param subreddit
     */
    void onLinkClick(String url, int xOffset, String subreddit, URLSpan span);

    void onLinkLongClick(String url, MotionEvent event);
}
