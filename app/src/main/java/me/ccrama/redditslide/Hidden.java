package me.ccrama.redditslide;

import android.os.AsyncTask;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by carlo_000 on 10/16/2015.
 */
public class Hidden {
    private static final Set<String> id = new HashSet<>();

    private static void setHiddenAsync(boolean hidden, final Submission contribution) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                try {

                    new AccountManager(Authentication.reddit).hide(hidden, contribution);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void setHidden(final Contribution s) {
        id.add(s.getFullName());
        setHiddenAsync(true, (Submission) s);
    }

    public static void undoHidden(final Contribution s) {
        id.remove(s.getFullName());
        setHiddenAsync(false, (Submission) s);
    }

    public static boolean getHidden(final String fullname) {
        return id.contains(fullname);
    }

}
