package me.ccrama.redditslide;

import android.database.Cursor;

import com.lusfold.androidkeyvaluestore.KVStore;
import com.lusfold.androidkeyvaluestore.core.KVManger;
import com.lusfold.androidkeyvaluestore.utils.CursorUtils;

import net.dean.jraw.models.Submission;

import java.util.HashMap;
import java.util.List;

import static com.lusfold.androidkeyvaluestore.core.KVManagerImpl.COLUMN_KEY;
import static com.lusfold.androidkeyvaluestore.core.KVManagerImpl.TABLE_NAME;

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

                // Check if KVStore has a key containing comments + the fullname
                // This is necessary because the KVStore library is limited and Carlos didn't realize the performance impact
                Cursor cur = m.execQuery("SELECT * FROM ? WHERE ? LIKE '%?%' LIMIT 1",
                        new String[] { TABLE_NAME, COLUMN_KEY, "comments" + fullname });
                boolean contains = cur != null && cur.getCount() > 0;
                CursorUtils.closeCursorQuietly(cur);

                if (contains) {
                    commentsSince.put(fullname, Integer.valueOf(m.get("comments" + fullname)));
                }
            }
        } catch(Exception ignored){

        }
    }

    public static int commentsSince(Submission s) {
        if (commentsSince != null && commentsSince.containsKey(s.getFullName()))
            return s.getCommentCount() - commentsSince.get(s.getFullName());
        return 0;
    }

    public static void setComments(Submission s) {
        if (commentsSince == null) {
            commentsSince = new HashMap<>();
        }
        KVStore.getInstance().insertOrUpdate("comments" + s.getFullName(), String.valueOf(s.getCommentCount()));
        commentsSince.put(s.getFullName(), s.getCommentCount());
    }
}
