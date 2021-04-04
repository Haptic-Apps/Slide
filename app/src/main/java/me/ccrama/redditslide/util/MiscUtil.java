package me.ccrama.redditslide.util;

import android.widget.TextView;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;

/**
 * Created by TacoTheDank on 03/15/2021.
 * <p>
 * These functions wouldn't really make sense to be anywhere else, so...
 * MiscUtil is meant to be temporary; these functions will ideally eventually go into their own little places.
 */
public class MiscUtil {

    // Used in SubredditView, MainActivity, and CommentPage (ugly-af code moment)
    public static void doSubscribeButtonText(boolean currentlySubbed, TextView subscribe) {
        if (Authentication.didOnline) {
            if (currentlySubbed) {
                subscribe.setText(R.string.unsubscribe_caps);
            } else {
                subscribe.setText(R.string.subscribe_caps);
            }
        } else {
            if (currentlySubbed) {
                subscribe.setText(R.string.btn_remove_from_sublist);
            } else {
                subscribe.setText(R.string.btn_add_to_sublist);
            }
        }
    }
}
