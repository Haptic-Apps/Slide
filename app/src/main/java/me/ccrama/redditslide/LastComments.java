package me.ccrama.redditslide;

import com.lusfold.androidkeyvaluestore.KVStore;
import com.lusfold.androidkeyvaluestore.core.KVManger;

import net.dean.jraw.models.Submission;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ccrama on 7/19/2015.
 */
public class LastComments {

    public static HashMap<String, Integer> commentsSince;

    public static void setCommentsSince(List<Submission> submissions) {
        if (commentsSince == null) {
            commentsSince = new HashMap<>();
        }
        KVManger m = KVStore.getInstance();
        try {
            for (Submission s : submissions) {
                String fullname = s.getFullName();
                if (!m.getByContains("comments" + fullname).isEmpty()) {
                    commentsSince.put(fullname, Integer.valueOf(m.get("comments" + fullname)));
                }
            }
        } catch (Exception ignored) {

        }
    }

    public static int commentsSince(Submission s) {
        if (commentsSince != null && commentsSince.containsKey(s.getFullName())) {
            return s.getCommentCount() - commentsSince.get(s.getFullName());
        }
        return 0;
    }

    public static void setComments(Submission s) {
        if (commentsSince == null) {
            commentsSince = new HashMap<>();
        }
        KVStore.getInstance()
                .insertOrUpdate("comments" + s.getFullName(), String.valueOf(s.getCommentCount()));
        commentsSince.put(s.getFullName(), s.getCommentCount());
    }
}
