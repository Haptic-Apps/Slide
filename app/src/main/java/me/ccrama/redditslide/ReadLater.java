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
                    .insert("readLater" + s.getFullName(), "true");
        } else {
            if (!KVStore.getInstance().getByContains("readLater" + s.getFullName()).isEmpty()) {
                KVStore.getInstance().delete("readLater" + s.getFullName());
            }
        }
    }
}
