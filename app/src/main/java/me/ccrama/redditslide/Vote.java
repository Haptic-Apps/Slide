package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.PublicContribution;
import net.dean.jraw.models.VoteDirection;

import java.lang.ref.WeakReference;

/**
 * Created by ccrama on 9/19/2015.
 */
public class Vote extends AsyncTask<PublicContribution, Void, Void> {

    private final VoteDirection direction;
    private WeakReference<View> v;
    private WeakReference<Context> c;


    public Vote(Boolean b, View v, Context c) {
        direction = b ? VoteDirection.UPVOTE : VoteDirection.DOWNVOTE;
        this.v = new WeakReference<>(v);
        this.c = new WeakReference<>(c);
        Reddit.setDefaultErrorHandler(c);
    }

    public Vote(View v, Context c) {
        direction = VoteDirection.NO_VOTE;
        this.v = new WeakReference<>(v);
        this.c = new WeakReference<>(c);
    }

    @Override
    protected Void doInBackground(PublicContribution... sub) {
        // Calling get() method just one time so it wont produce NPEs
        // As subsequent access may produce NPEs
        View view = v.get();
        Context context = null;
        if (Authentication.isLoggedIn) {
            try {
                new AccountManager(Authentication.reddit).vote(sub[0], direction);
            } catch (ApiException | RuntimeException e) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if (view != null && context != null) {
                                Snackbar s = Snackbar.make(view, R.string.vote_err, Snackbar.LENGTH_SHORT);
                                View view = s.getView();
                                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                tv.setTextColor(Color.WHITE);
                                s.show();
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
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        if (view != null && context != null) {
                            Snackbar s = Snackbar.make(view, R.string.vote_err_login, Snackbar.LENGTH_SHORT);
                            View view = s.getView();
                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();

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

