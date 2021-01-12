package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.VoteDirection;

import me.ccrama.redditslide.util.LayoutUtils;

/**
 * Created by ccrama on 9/19/2015.
 */
public class Vote extends AsyncTask<PublicContribution, Void, Void> {

    private final VoteDirection direction;
    private View v;
    private Context c;

    public Vote(Boolean b, View v, Context c) {
        direction = b ? VoteDirection.UPVOTE : VoteDirection.DOWNVOTE;
        this.v = v;
        this.c = c;
        Reddit.setDefaultErrorHandler(c);

    }

    public Vote(View v, Context c) {

        direction = VoteDirection.NO_VOTE;

        this.v = v;
        this.c = c;

    }

    @Override
    protected Void doInBackground(PublicContribution... sub) {

        if (Authentication.isLoggedIn) {
            try {
                new AccountManager(Authentication.reddit).vote(sub[0], direction);
            } catch (ApiException | RuntimeException e) {
                ((Activity) c).runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if (v != null && c != null && v.getContext() != null) {
                                Snackbar s = Snackbar.make(v, R.string.vote_err, Snackbar.LENGTH_SHORT);
                                LayoutUtils.showSnackbar(s);
                            }
                        } catch (Exception ignored) {

                        }
                        c = null;
                        v = null;
                    }
                });
                e.printStackTrace();
            }
        } else {
            ((Activity) c).runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        if (v != null && c != null && v.getContext() != null) {
                            Snackbar s = Snackbar.make(v, R.string.vote_err_login, Snackbar.LENGTH_SHORT);
                            LayoutUtils.showSnackbar(s);
                        }
                    } catch (Exception ignored) {

                    }
                    c = null;
                    v = null;
                }
            });
        }


        return null;


    }


}

