package me.ccrama.redditslide;

import android.database.Cursor;
import android.net.Uri;

import com.lusfold.androidkeyvaluestore.KVStore;
import com.lusfold.androidkeyvaluestore.core.KVManger;
import com.lusfold.androidkeyvaluestore.utils.CursorUtils;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import me.ccrama.redditslide.Synccit.SynccitRead;

import static com.lusfold.androidkeyvaluestore.core.KVManagerImpl.COLUMN_KEY;
import static com.lusfold.androidkeyvaluestore.core.KVManagerImpl.TABLE_NAME;
import static me.ccrama.redditslide.OpenRedditLink.formatRedditUrl;
import static me.ccrama.redditslide.OpenRedditLink.getRedditLinkType;

/**
 * Created by ccrama on 7/19/2015.
 */
public class HasSeen {

    public static HashSet<String>       hasSeen;
    public static HashMap<String, Long> seenTimes;

    public static void setHasSeenContrib(List<Contribution> submissions) {
        if (hasSeen == null) {
            hasSeen = new HashSet<>();
            seenTimes = new HashMap<>();
        }
        KVManger m = KVStore.getInstance();
        for (Contribution s : submissions) {
            if (s instanceof Submission) {
                String fullname = s.getFullName();
                if (fullname.contains("t3_")) {
                    fullname = fullname.substring(3);
                }

                // Check if KVStore has a key containing the fullname
                // This is necessary because the KVStore library is limited and Carlos didn't realize the performance impact
                Cursor cur = m.execQuery("SELECT * FROM ? WHERE ? LIKE '%?%' LIMIT 1",
                        new String[] { TABLE_NAME, COLUMN_KEY, fullname });
                boolean contains = cur != null && cur.getCount() > 0;
                CursorUtils.closeCursorQuietly(cur);

                if (contains) {
                    hasSeen.add(fullname);
                    String value = m.get(fullname);
                    try {
                        if (value != null) seenTimes.put(fullname, Long.valueOf(value));
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    public static void setHasSeenSubmission(List<Submission> submissions) {
        if (hasSeen == null) {
            hasSeen = new HashSet<>();
            seenTimes = new HashMap<>();
        }
        KVManger m = KVStore.getInstance();
        for (Contribution s : submissions) {
            String fullname = s.getFullName();
            if (fullname.contains("t3_")) {
                fullname = fullname.substring(3);
            }
            // Check if KVStore has a key containing the fullname
            // This is necessary because the KVStore library is limited and Carlos didn't realize the performance impact
            Cursor cur = m.execQuery("SELECT * FROM ? WHERE ? LIKE '%?%' LIMIT 1",
                    new String[] { TABLE_NAME, COLUMN_KEY, fullname });
            boolean contains = cur != null && cur.getCount() > 0;
            CursorUtils.closeCursorQuietly(cur);

            if (contains) {
                hasSeen.add(fullname);
                String value = m.get(fullname);
                try {
                    if (value != null) seenTimes.put(fullname, Long.valueOf(value));
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static boolean getSeen(Submission s) {
        if (hasSeen == null) {
            hasSeen = new HashSet<>();
            seenTimes = new HashMap<>();
        }

        String fullname = s.getFullName();
        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3);
        }
        return (hasSeen.contains(fullname)
                || SynccitRead.visitedIds.contains(fullname)
                || s.getDataNode().has("visited") && s.getDataNode().get("visited").asBoolean()
                || s.getVote() != VoteDirection.NO_VOTE);
    }

    public static boolean getSeen(String s) {
        if (hasSeen == null) {
            hasSeen = new HashSet<>();
            seenTimes = new HashMap<>();
        }

        Uri uri = formatRedditUrl(s);
        String fullname = s;
        if (uri != null) {
            String host = uri.getHost();

            if (host.startsWith("np")) {
                uri = uri.buildUpon().authority(host.substring(2)).build();
            }

            OpenRedditLink.RedditLinkType type = getRedditLinkType(uri);
            List<String> parts = uri.getPathSegments();

            switch (type) {
                case SHORTENED: {
                    fullname = parts.get(0);
                    break;
                }
                case COMMENT_PERMALINK:
                case SUBMISSION: {
                    fullname = parts.get(3);
                    break;
                }
                case SUBMISSION_WITHOUT_SUB: {
                    fullname = parts.get(1);
                    break;
                }
            }
        }

        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3);
        }
        hasSeen.add(fullname);
        return (hasSeen.contains(fullname) || SynccitRead.visitedIds.contains(fullname));
    }

    public static long getSeenTime(Submission s) {
        if (hasSeen == null) {
            hasSeen = new HashSet<>();
            seenTimes = new HashMap<>();
        }
        String fullname = s.getFullName();
        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3);
        }
        if (seenTimes.containsKey(fullname)) {
            return seenTimes.get(fullname);
        } else {
            try {
                return Long.parseLong(KVStore.getInstance().get(fullname));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    public static void addSeen(String fullname) {
        if (hasSeen == null) {
            hasSeen = new HashSet<>();
        }
        if (seenTimes == null) {
            seenTimes = new HashMap<>();
        }

        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3);
        }

        hasSeen.add(fullname);
        seenTimes.put(fullname, System.currentTimeMillis());

        long result =
                KVStore.getInstance().insert(fullname, String.valueOf(System.currentTimeMillis()));
        if (result == -1) {
            KVStore.getInstance().update(fullname, String.valueOf(System.currentTimeMillis()));
        }

        if (!fullname.contains("t1_")) {
            SynccitRead.newVisited.add(fullname);
            SynccitRead.visitedIds.add(fullname);
        }
    }

    public static void addSeenScrolling(String fullname) {
        if (hasSeen == null) {
            hasSeen = new HashSet<>();
        }
        if (seenTimes == null) {
            seenTimes = new HashMap<>();
        }

        if (fullname.contains("t3_")) {
            fullname = fullname.substring(3);
        }

        hasSeen.add(fullname);
        seenTimes.put(fullname, System.currentTimeMillis());

        KVStore.getInstance().insert(fullname, String.valueOf(System.currentTimeMillis()));

        if (!fullname.contains("t1_")) {
            SynccitRead.newVisited.add(fullname);
            SynccitRead.visitedIds.add(fullname);
        }
    }
}
