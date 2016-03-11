package me.ccrama.redditslide;

import net.dean.jraw.models.Submission;

/**
 * Created by ccrama on 7/19/2015.
 */
public class LastComments {

    public static int commentsSince(Submission s) {
        if (Reddit.seen.contains("comments" + s.getFullName())) {
            return s.getCommentCount() - Reddit.seen.getInt("comments" + s.getFullName(), s.getCommentCount());
        }
        return 0;
    }

    public static void setComments(Submission s) {
        Reddit.seen.edit().putInt("comments" + s.getFullName(), s.getCommentCount()).apply();
    }
}
