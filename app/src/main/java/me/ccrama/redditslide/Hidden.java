package me.ccrama.redditslide;

import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by carlo_000 on 10/16/2015.
 */
public class Hidden {
    private static final Set<String> id = new HashSet<>();
    private static final HashMap<String, Submission> hideQueue = new HashMap<>();

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

    public static void addSubmissionToHideQueue(final Submission submission) {
        if (hideQueue.containsKey(submission.getFullName())) {
            return;
        }
        Log.i("HideDebug", "Added " + submission.getFullName() + " to hide queue");
        hideQueue.put(submission.getFullName(), submission);
    }

    public static AsyncTask<Void, Void, Void> asyncHideQueue() {
        if (hideQueue.isEmpty()) {
            return null;
        }

        final ArrayList<Submission> submissions = new ArrayList<>(hideQueue.values());
        final Submission firstSubmission = submissions.get(0);
        final Submission[] remainingSubmissions = submissions.subList(1, submissions.size()).toArray(new Submission[0]);
        hideQueue.clear();

        return new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                try {
                    new AccountManager(Authentication.reddit).hide(true, firstSubmission, remainingSubmissions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
