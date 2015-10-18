package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.VoteDirection;

/**
 * Created by ccrama on 9/19/2015.
 */
public class Vote extends AsyncTask<PublicContribution, Void, Void> {

    VoteDirection direction;

    public Vote(Boolean b, View v, Context c) {
        if (b) {
            direction = VoteDirection.UPVOTE;
        } else {
            direction = VoteDirection.DOWNVOTE;
        }
        this.v = v;
        this.c = c;

    }

    View v;
    Context c;

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
                ((Activity) c).runOnUiThread(new Runnable() {
                    public void run() {
                        Snackbar.make(v, "Vote cast!", Snackbar.LENGTH_SHORT).show();
                        c = null;
                        v = null;
                    }
                });
            } catch (ApiException e) {
                ((Activity) c).runOnUiThread(new Runnable() {
                    public void run() {
                        Snackbar.make(v, "Error casting vote!", Snackbar.LENGTH_SHORT).show();
                        c = null;
                        v = null;
                    }
                });
                e.printStackTrace();
            }
        } else {
            ((Activity) c).runOnUiThread(new Runnable() {
                public void run() {
                    Snackbar.make(v, "You must be logged in to vote!", Snackbar.LENGTH_SHORT).show();
                    c = null;
                    v = null;
                }
            });
        }


        return null;


    }


}

