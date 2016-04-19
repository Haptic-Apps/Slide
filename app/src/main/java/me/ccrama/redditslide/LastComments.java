package me.ccrama.redditslide;

import com.lusfold.androidkeyvaluestore.KVStore;

import net.dean.jraw.models.Submission;

/**
 * Created by ccrama on 7/19/2015.
 */
public class LastComments {

    public static int commentsSince(Submission s) {

        if (!KVStore.getInstance().getByContains("comments" + s.getFullName()).isEmpty()) {
            return s.getCommentCount() - Integer.valueOf(KVStore.getInstance().get("comments" + s.getFullName()));
        }
        return 0;
    }

    public static void setComments(Submission s) {
        KVStore.getInstance().insert("comments" + s.getFullName(), String.valueOf(s.getCommentCount()));
    }
}
