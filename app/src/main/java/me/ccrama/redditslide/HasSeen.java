package me.ccrama.redditslide;

import com.lusfold.androidkeyvaluestore.KVStore;
import com.lusfold.androidkeyvaluestore.core.KVManger;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.ccrama.redditslide.Synccit.SynccitRead;

/**
 * Created by ccrama on 7/19/2015.
 */
public class HasSeen {

    public static ArrayList<String> hasSeen;
    public static HashMap<String, Long> seenTimes;

    public static void setHasSeenContrib(List<Contribution> submissions) {
        if (hasSeen == null) {
            hasSeen = new ArrayList<>();
            seenTimes = new HashMap<>();
        }
        KVManger m = KVStore.getInstance();
        for (Contribution s : submissions) {
            if (s instanceof Submission) {
                String fullname = s.getFullName();
                if (fullname.contains("t3_")) {
                    fullname = fullname.substring(3, fullname.length());
                }
                if (!m.getByContains(fullname).isEmpty() && m.get(fullname) != null) {
                    hasSeen.add(fullname);
                    try {
                        seenTimes.put(fullname, Long.valueOf(m.get(fullname)));
                    } catch(Exception e){

                    }
                }
            }
        }
    }

    public static void setHasSeenSubmission(List<Submission> submissions) {
        if (hasSeen == null) {
            hasSeen = new ArrayList<>();
            seenTimes = new HashMap<>();
        }
        KVManger m = KVStore.getInstance();
        for (Contribution s : submissions) {
            String fullname = s.getFullName();
            if (fullname.contains("t3_")) {
                fullname = fullname.substring(3, fullname.length());
            }
            if (!m.getByContains(fullname).isEmpty()) {
                hasSeen.add(fullname);
                String value = m.get(fullname);
                try {
                    if (value != null)
                        seenTimes.put(fullname, Long.valueOf(value));
                } catch (Exception ignored) {
                }

            }
        }
    }

    public static boolean getSeen(Submission s) {
        if (hasSeen == null) {
            hasSeen = new ArrayList<>();
            seenTimes = new HashMap<>();
        }
        if (s.getDataNode().has("visited") && s.getDataNode().get("visited").asBoolean() || s.getVote() != VoteDirection.NO_VOTE) {
            return true;
        }
        String fullname = s.getFullName();
        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3, fullname.length());
        }
        return hasSeen.contains(fullname) || SynccitRead.visitedIds.contains(fullname);
    }

    public static long getSeenTime(Submission s) {
        if (hasSeen == null) {
            hasSeen = new ArrayList<>();
            seenTimes = new HashMap<>();
        }
        String fullname = s.getFullName();
        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3, fullname.length());
        }
        return seenTimes.containsKey(fullname) ? seenTimes.get(fullname) : System.currentTimeMillis();
    }

    public static void addSeen(String fullname) {
        if (hasSeen == null) {
            hasSeen = new ArrayList<>();
            seenTimes = new HashMap<>();
        }
        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3, fullname.length());
        }

        hasSeen.add(fullname);

        KVStore.getInstance().insert(fullname, String.valueOf(System.currentTimeMillis()));

        if (!fullname.contains("t1_")) {
            SynccitRead.newVisited.add(fullname);
            SynccitRead.visitedIds.add(fullname);
        }
    }
}
