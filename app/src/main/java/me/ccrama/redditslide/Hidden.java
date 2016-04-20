package me.ccrama.redditslide;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

/**
 * Created by carlo_000 on 10/16/2015.
 */
public class Hidden {
    public static SharedPreferences hidden;


    public static void setHidden(final Contribution s) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                try {
                    new AccountManager(Authentication.reddit).hide(true, (Submission)s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
        hidden.edit().putBoolean(s.getFullName(), true).apply();
    }

    public static void undoHidden(final Contribution s) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void[] params) {
                try {
                    new AccountManager(Authentication.reddit).hide(false, (Submission)s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

}
