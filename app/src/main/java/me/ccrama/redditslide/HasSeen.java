package me.ccrama.redditslide;

import com.lusfold.androidkeyvaluestore.KVStore;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Synccit.SynccitRead;

/**
 * Created by ccrama on 7/19/2015.
 */
public class HasSeen {

    public static boolean getSeen(Submission s) {

        if (s.getDataNode().has("visited") && s.getDataNode().get("visited").asBoolean()) {
            return true;
        }
        String fullname = s.getFullName();
        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3, fullname.length());
        }
        return !KVStore.getInstance().getByContains(fullname).isEmpty() || SynccitRead.visitedIds.contains(fullname);
    }

    public static void addSeen(String fullname) {
        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3, fullname.length());
        }
        KVStore.getInstance().insert(fullname, String.valueOf(System.currentTimeMillis()));

        if(!fullname.contains("t1_")) {
            SynccitRead.newVisited.add(fullname);
            SynccitRead.visitedIds.add(fullname);
        }
    }
}
