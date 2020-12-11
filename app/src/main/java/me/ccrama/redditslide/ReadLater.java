package me.ccrama.redditslide;

import com.lusfold.androidkeyvaluestore.KVStore;

import net.dean.jraw.models.Submission;

/**
 * Created by ccrama on 7/19/2015.
 */
public class ReadLater {

    public static void setReadLater(Submission s, boolean readLater) {
        if (readLater) {
            KVStore.getInstance()
                    .insert("readLater" + s.getFullName(), String.valueOf(System.currentTimeMillis()));
        } else {
            if (isToBeReadLater(s)) {
                KVStore.getInstance().delete("readLater" + s.getFullName());
            }
        }
    }

    public static boolean isToBeReadLater(Submission s) {
        return !KVStore.getInstance().getByContains("readLater" + s.getFullName()).isEmpty();
    }
}
